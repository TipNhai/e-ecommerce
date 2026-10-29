package com.example.demo.service;

import com.example.demo.dto.request.CreateProductRequest;
import com.example.demo.dto.request.ProductFilterParam;
import com.example.demo.dto.request.UpdateProductRequest;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.ProductDetailResponse;
import com.example.demo.dto.response.ProductImageResponse;
import com.example.demo.dto.response.ProductSummaryResponse;
import com.example.demo.entity.Brand;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.enums.AuditAction;
import com.example.demo.event.AuditEvent;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.ProductImageMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repository.*;
import com.example.demo.specification.ProductSpecification;
import com.example.demo.utils.SlugUtils;
import com.example.demo.config.BloomFilterManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductImageRepository productImageRepository;
    ProductImageMapper productImageMapper;
    RedisTemplate<String , Object> redisTemplate;
    KafkaTemplate<String , Object> kafkaTemplate;
    RedissonClient redissonClient;
    BloomFilterManager bloomFilterManager;
    @Cacheable(value = "product_pages", key = "#param.hashCode()", sync = true)
    public PageResponse<ProductSummaryResponse> getAllProducts(ProductFilterParam param , String userId){
        Specification<Product>specification = ProductSpecification.filter(param);
        if(param.getSearch() != null && !param.getSearch().trim().isEmpty() && userId != null){
            String searchKey = "user:searches:" + userId;
            String keyword = param.getSearch().trim();
            //XÓA các từ khóa trùng lặp trước (0 có nghĩa là xóa sạch tất cả các chữ giống hệt trong List)
            redisTemplate.opsForList().remove(searchKey, 0, keyword);
            //Đẩy từ khóa mới vào bên trái (đầu danh sách)
            redisTemplate.opsForList().leftPush(searchKey, keyword);
            //Cắt bớt, chỉ giữ lại index từ 0 đến 4 (5 phần tử mới nhất)
            redisTemplate.opsForList().trim(searchKey, 0, 4);
            //Quản lý tự động xoá trong 30 ngày
            redisTemplate.expire(searchKey, 30, TimeUnit.DAYS);
        }
        Sort sort = Sort.unsorted();
        if(StringUtils.hasText(param.getSortBy())){
            String dir = param.getSortDir();
            sort = "desc".equalsIgnoreCase(dir)
                    ? Sort.by(param.getSortBy()).descending()
                    : Sort.by(param.getSortBy()).ascending();
        }
        int pageNo = param.getPageNo() != null ? param.getPageNo() : 0;
        int pageSize = param.getPageSize() != null ? param.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageNo,pageSize,sort);
        Page<Product> products = productRepository.findAll(specification, pageable);
        Page<ProductSummaryResponse> productSummaryResponsePage = products.map(productMapper::toSummaryResponse);
        return PageResponse.from(productSummaryResponsePage);
    }

    //Double check dùng tránh Stampede
    public ProductSummaryResponse getProduct(Long id){
        // 0. CHẶN ĐỨNG CACHE PENETRATION TẠI ĐÂY (Bloom Filter)
        if (!bloomFilterManager.mightContainProduct(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 1. Tăng lượt xem lên
        String viewKey = "product:views:" + id;
        redisTemplate.opsForValue().increment(viewKey);

        //Kiểm tra Cache (tự làm để tránh lỗi Proxy của @Cacheable)
        String cacheKey = "products::" + id;
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return (ProductSummaryResponse) cachedData;
        }
        // Cache Miss -> Dùng Redisson Lock để chỉ cho phép 1 luồng duy nhất xuống DB nạp lại Cache
        String lockKey = "lock:stampede:product:" + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isAcquired = false;

        try {
            // Chờ tối đa 3s để lấy Lock, tự nhả Lock sau 5s
            isAcquired = lock.tryLock(3, 5, TimeUnit.SECONDS);

            if(!isAcquired){
                // Luồng khác chưa lấy được lock -> Chờ 50ms rồi thử đọc lại từ Cache (vừa được luồng trước nạp)
                Thread.sleep(50);
                Object retryCache = redisTemplate.opsForValue().get(cacheKey);
                return (ProductSummaryResponse) retryCache;
            }

            // Lần 2 (Double Check Cache): Kiểm tra lại xem luồng trước đó đã nạp Cache chưa
            Object doubleCheckCache = redisTemplate.opsForValue().get(cacheKey);
            if(doubleCheckCache != null){
                return (ProductSummaryResponse) doubleCheckCache;
            }
        //Nếu chưa có Cache -> Xuống DB lấy
        var product = productRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductSummaryResponse response = productMapper.toSummaryResponse(product);

        //Lưu vào Cache cho lần sau
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(60));

        return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Xảy ra lỗi gián đoạn luồng khi chờ Lock Stampede", e);
        }finally {
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request){
        var slug = SlugUtils.makeSlug(request.getName());
        if(productRepository.existsBySlug(slug)){
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        Product product = productMapper.createToEntity(request);
//        TỐI ƯU DB: Dùng getReferenceById để lấy Proxy Entity (KHÔNG bắn SQL SELECT)
//        Category category = categoryRepository.getReferenceById(request.getCategoryId());
//        Brand brand = brandRepository.getReferenceById(request.getBrandId());
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        Brand brand = brandRepository.findById(request.getBrandId()).orElseThrow(()-> new AppException(ErrorCode.BRAND_NOT_FOUND));
        product.setCategory(category);
        product.setBrand(brand);
        product.setSlug(slug);
        productRepository.save(product);
        bloomFilterManager.addProduct(product.getId());
        return productMapper.toDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long productId,UpdateProductRequest request,String adminId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        //Cập nhật lại slug
        if(StringUtils.hasText(request.getName())){
            product.setSlug(SlugUtils.makeSlug(request.getName()));
        }
        productMapper.updateToEntity(request, product);
        if(request.getCategoryId()!=null){
            var category=categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }
        if(request.getBrandId()!=null) {
            var brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            product.setBrand(brand);
        }
        productRepository.save(product);

        AuditEvent auditEvent = new AuditEvent(adminId , AuditAction.UPDATE, "PRODUCT",String.valueOf(productId),"Gia moi: "+request.getPrice());
        kafkaTemplate.send("audit_events_topic",adminId,auditEvent);
        //Cập nhật xong xóa Cache cũ đi
        redisTemplate.delete("products::" + productId);
        
        return productMapper.toDetailResponse(product);
    }


    @Transactional
    public void deleteProduct(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);

        redisTemplate.delete("products::" + id);
    }

    // Cập nhật nhanh số lượng tồn kho (Thường được gọi nội bộ khi Đặt hàng / Hủy đơn) , còn nên dùng redis để tối ưu
    @Transactional
    public void updateQuantity(Long productId, int quantityDelta){
        productRepository.updateQuantity(productId, quantityDelta);

        redisTemplate.delete("products::" + productId);
    }

    // 9. Upload / Thêm danh sách ảnh phụ cho sản phẩm
    @Transactional
    public List<ProductImageResponse> addProductImages(Long productId, List<String> imageUrls){
        if(imageUrls == null || imageUrls.isEmpty()){
            return Collections.emptyList();
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        List<ProductImage> productImages = imageUrls.stream()
                        .map(url -> ProductImage.builder()
                                .image(url)
                                .product(product)
                                .build())
                        .toList();

        redisTemplate.delete("products::" + productId);
        return productImageRepository.saveAll(productImages).stream()
                .map(productImageMapper::toProductImageResponse)
                .toList();
    }

    @Transactional
    public void deleteProductImage(Long productId, Long imageId){
        //Tránh xoá nhầm ảnh của sản phẩm khác
        ProductImage productImage = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        productImageRepository.delete(productImage);

        redisTemplate.delete("products::" + productId);
    }

    @Transactional
    public void setProductThumbnail(Long productId, Long imageId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        if(!productImage.getProduct().getId().equals(productId))
            throw new AppException(ErrorCode.INVALID_DATA);
        product.setThumbnail(productImage.getImage());

        redisTemplate.delete("products::" + productId);

        productRepository.save(product);
    }

    public List<Object> getRecentSearches(String userId) {
        if (userId == null) return Collections.emptyList();
        String searchKey = "user:searches:" + userId;
        // Lấy từ index 0 đến -1 (lấy toàn bộ list)
        return redisTemplate.opsForList().range(searchKey, 0, -1);
    }

    public List<ProductSummaryResponse> getTrendingProducts(int limit) {
        String currentMonth = YearMonth.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        String leaderboardKey = "trending:products:" + currentMonth;

        // 1. Ra lệnh Redis: Lấy Top (limit) sản phẩm điểm cao nhất (từ 0 đến limit - 1)
        // reverseRange nghĩa là lấy xếp hạng từ cao xuống thấp
        Set<Object> topProductIds = redisTemplate.opsForZSet().reverseRange(leaderboardKey, 0, limit - 1L);

        if (topProductIds == null || topProductIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Chuyển đổi Set<Object> thành List<Long>
        List<Long> productIds = topProductIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        // Lấy thông tin chi tiết từ DB
        // findAllById của Spring Data JPA không giữ đúng thứ tự.
        // Tốt nhất nên viết một @Query tùy chỉnh hoặc sắp xếp lại list sau khi lấy.
        List<Product> products = productRepository.findAllById(productIds);

        // Khôi phục lại đúng thứ tự Top 1 -> Top N
        List<Product> sortedProducts = new ArrayList<>();
        for (Long id : productIds) {
            products.stream().filter(p -> p.getId().equals(id)).findFirst().ifPresent(sortedProducts::add);
        }

        // 4. Map sang Response
        return sortedProducts.stream()
                .map(productMapper::toSummaryResponse)
                .toList();
    }

}
