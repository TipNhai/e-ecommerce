package com.example.demo.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // ==========================================
    // SYSTEM & GENERAL ERRORS (9000 - 9999)
    // ==========================================
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9001, "Invalid message key", HttpStatus.BAD_REQUEST),
    NOT_FOUND(9002, "Resource not found", HttpStatus.NOT_FOUND),
    EMPTY(9003, "Resource is empty", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(9004,"Invalid date range" ,HttpStatus.BAD_REQUEST ),
    NOTIFICATION_NOT_FOUND(9005,"Notification not found" ,HttpStatus.BAD_REQUEST ),
    INVALID_DATA(9006,"Data not accessed" ,HttpStatus.BAD_REQUEST ),
    TOO_MANY_REQUEST(9007,"Too many request" ,HttpStatus.TOO_MANY_REQUESTS ),
    // ==========================================
    // 1. IDENTITY & AUTH (1000 - 1999)
    // ==========================================
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_EXISTED(1003, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
    INVALID_USERNAME(1005, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1006, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1007, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1008, "Old password does not match", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1009, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1010, "Role already exists", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(1011, "Permission not found", HttpStatus.NOT_FOUND),
    INVALID_REFRESH_TOKEN(1012, "Refresh token is invalid or expired", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 2. CATALOG (2000 - 2999)
    // ==========================================
    // Products
    PRODUCT_NOT_FOUND(2001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_EXISTED(2002, "Product already exists", HttpStatus.BAD_REQUEST),
    PRODUCT_OUT_OF_STOCK(2003, "Product is out of stock", HttpStatus.BAD_REQUEST),

    // Categories
    CATEGORY_NOT_FOUND(2101, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_EXISTED(2102, "Category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_HAS_PRODUCTS(2103, "Cannot delete category containing products", HttpStatus.BAD_REQUEST),

    // Brands
    BRAND_NOT_FOUND(2201, "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_EXISTED(2202, "Brand already exists", HttpStatus.BAD_REQUEST),

    // Product Images
    IMAGE_NOT_FOUND(2301, "Product image not found", HttpStatus.NOT_FOUND),
    IMAGE_LIMIT_EXCEEDED(2302, "Maximum product images exceeded", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 3. CUSTOMER (3000 - 3999)
    // ==========================================
    // Cart & Cart Items
    CART_NOT_FOUND(3001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(3002, "Cart item not found", HttpStatus.NOT_FOUND),
    INVALID_QUANTITY(3003, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),
    QUANTITY_EXCEEDS_STOCK(3004, "Requested quantity exceeds available stock", HttpStatus.BAD_REQUEST),

    // Address
    ADDRESS_NOT_FOUND(3101, "Address not found", HttpStatus.NOT_FOUND),
    MAX_ADDRESS_LIMIT(3102, "Maximum address limit reached", HttpStatus.BAD_REQUEST),

    // Favorites
    FAVORITE_NOT_FOUND(3201, "Favorite list not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_FAVORITED(3202, "Product is already in favorites", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 4. ORDER (4000 - 4999)
    // ==========================================
    ORDER_NOT_FOUND(4001, "Order not found", HttpStatus.NOT_FOUND),
    EMPTY_CART(4002, "Cannot create order from an empty cart", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(4003, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    ORDER_CANNOT_BE_CANCELLED(4004, "Order cannot be cancelled at this stage", HttpStatus.BAD_REQUEST),
    ORDER_NOT_DELIVERED(4005,"Order have not been delivered" ,HttpStatus.BAD_REQUEST ),
    PRODUCT_NOT_IN_ORDER(4006,"Product not in order" ,HttpStatus.BAD_REQUEST ),

    // ==========================================
    // 5. REVIEW (5000 - 5999)
    // ==========================================
    REVIEW_NOT_FOUND(5001, "Review not found", HttpStatus.NOT_FOUND),
    CANNOT_REVIEW_UNPURCHASED_PRODUCT(5002, "You can only review products you have purchased", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(5003, "You have already reviewed this product", HttpStatus.BAD_REQUEST),

    // ==========================================
    // 6. PAYMENT (6000 - 6999)
    // ==========================================
    PAYMENT_NOT_FOUND(6001, "Payment transaction not found", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED(6002, "Payment processing failed", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_AMOUNT(6003, "Invalid payment amount", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_COMPLETED(6004, "Order has already been paid", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_CHECKSUM(6005,"Signature Invalid" ,HttpStatus.BAD_REQUEST ),
    PAYMENT_GATEWAY_ERROR(6006,"Payment gateway error" ,HttpStatus.BAD_REQUEST ),
    UNSUPPORTED_PAYMENT_METHOD(6007,"Unsupported payment method" ,HttpStatus.BAD_REQUEST ),
    // ==========================================
    // 7. Code (7000 - 7999)
    // ==========================================
    COUPON_ALREADY_EXISTS(7001,"Coupon already exist" ,HttpStatus.BAD_REQUEST ),
    COUPON_NOT_FOUND(7002,"Coupon not found" ,HttpStatus.BAD_REQUEST ),
    COUPON_NOT_APPLICABLE(7003,"Coupon not applicable" ,HttpStatus.BAD_REQUEST ),

    // ==========================================
    // 8. File (8000 - 8999)
    // ==========================================
    FILE_DELETE_FAILED(8001,"File delete fail" ,HttpStatus.BAD_REQUEST ),
    FILE_IS_EMPTY(8002,"File is empty" ,HttpStatus.BAD_REQUEST ),
    FILE_SIZE_EXCEEDED(8003,"File size exceeded" ,HttpStatus.BAD_REQUEST ),
    INVALID_FILE_TYPE(8004,"Invalid file type" ,HttpStatus.BAD_REQUEST ),
    FILE_UPLOAD_FAILED(8005,"File upload failed" ,HttpStatus.BAD_REQUEST );


    int code;
    String message;
    HttpStatusCode httpStatusCode;
    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
