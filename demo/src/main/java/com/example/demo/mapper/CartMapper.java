package com.example.demo.mapper;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.dto.response.CartResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "productId" , source = "product.id")
    @Mapping(target = "productName" , source = "product.name")
    @Mapping(target = "productThumbnail" , source = "product.thumbnail")
    @Mapping(target = "unitPrice" , source = "product.price")
    @Mapping(target = "subTotal" , source = ".", qualifiedByName = "calculateSubTotal")
    CartItemResponse toCartItemResponse(CartItem cartItem);


    @Mapping(target = "userId" , source = "user.id")
    @Mapping(target = "cartItems" , source = "cartItems")
    @Mapping(target = "totalQuantity" , source = "cartItems", qualifiedByName = "calculateTotalQuantity")
    @Mapping(target = "totalPrice" , source = "cartItems", qualifiedByName = "calculateTotalPrice")
    CartResponse toCartResponse(Cart cart);

    //Hàm tính toán Custom
    @Named("calculateSubTotal")
    default BigDecimal calculateSubTotal(CartItem cartItem){
        if(cartItem == null || cartItem.getProduct() == null || cartItem.getQuantity() == null)
            return BigDecimal.ZERO;
        return cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    @Named("calculateTotalQuantity")
    default Integer calculateTotalQuantity(List<CartItem> cartItems){
        if(cartItems == null || cartItems.isEmpty())
            return 0;
        return cartItems.stream()
                .filter(item -> item != null && item.getQuantity() != null)
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Named("calculateTotalPrice")
    default BigDecimal calculateTotalPrice(List<CartItem> cartItems){
        if(cartItems == null || cartItems.isEmpty() )
            return BigDecimal.ZERO;
        return cartItems.stream()
                .map(this::calculateSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
