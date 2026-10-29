package com.example.demo.mapper;

import com.example.demo.dto.request.CreateOrderRequest;
import com.example.demo.dto.response.OrderItemResponse;
import com.example.demo.dto.response.OrderResponse;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "orderItems" , source = "orderItems")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "productId" , source = "product.id")
    @Mapping(target = "subTotal" ,source = "." , qualifiedByName = "calculateItemSubTotal")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", source = "product")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumbnail", source = "product.thumbnail")
    @Mapping(target = "price", source = "product.price") // Đóng băng giá niêm yết hiện tại
    @Mapping(target = "quantity", source = "quantity")
    OrderItem toOrderItem(CartItem cartItem);

    @Named("calculateItemSubTotal")
    default BigDecimal calculateItemSubTotal(OrderItem item){
        if(item == null || item.getPrice()==null || item.getQuantity()==null){
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}
