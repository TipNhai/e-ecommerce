package com.example.demo.config;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;

// Tránh tấn công penetration khi cố gửi giá trị không có trong Database
@Slf4j
@Service
public class BloomFilterManager {
    private final RedissonClient redissonClient;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    private RBloomFilter<String> userBloomFilter;
    private RBloomFilter<Long> productBloomFilter;

    public BloomFilterManager(RedissonClient redissonClient, 
                              UserRepository userRepository, 
                              ProductRepository productRepository) {
        this.redissonClient = redissonClient;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void init() {
        // 1. Khởi tạo Bloom Filter cho User
        userBloomFilter = redissonClient.getBloomFilter("userBloomFilter");
        userBloomFilter.tryInit(100000L, 0.01);
        List<User> users = userRepository.findAll();
        users.forEach(user -> userBloomFilter.add(user.getId()));
        log.info("==> [BloomFilter] Đã nạp {} User vào userBloomFilter", users.size());

        // 2. Khởi tạo Bloom Filter cho Product
        productBloomFilter = redissonClient.getBloomFilter("productBloomFilter");
        productBloomFilter.tryInit(100000L, 0.01);
        List<Product> products = productRepository.findAll();
        products.forEach(product -> productBloomFilter.add(product.getId()));
        log.info("==> [BloomFilter] Đã nạp {} Product vào productBloomFilter", products.size());
    }

    // === HÀM DÀNH CHO USER ===
    public boolean mightContainUser(String userId) {
        return userBloomFilter.contains(userId);
    }

    public void addUser(String userId) {
        userBloomFilter.add(userId);
    }

    // === HÀM DÀNH CHO PRODUCT ===
    public boolean mightContainProduct(Long productId) {
        if (productId == null) return false;
        boolean exists = productBloomFilter.contains(productId);
        if (!exists) {
            log.warn("==> [BloomFilter CHẶN ĐỨNG] Product ID [{}] không tồn tại, chặn ngay không cho chạm DB!", productId);
        }
        return exists;
    }

    public void addProduct(Long productId) {
        if (productId != null) {
            productBloomFilter.add(productId);
            log.info("==> [BloomFilter] Đã thêm Product ID [{}] vào filter", productId);
        }
    }

    public long getProductCount() {
        return productBloomFilter.count();
    }
}
