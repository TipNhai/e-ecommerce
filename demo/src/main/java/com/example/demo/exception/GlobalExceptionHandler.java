package com.example.demo.exception;

import com.example.demo.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

@ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse>handlerRuntimeException(RuntimeException exception){
    log.error("Unhandled exception caught: ", exception);
    ApiResponse response = ApiResponse.builder()
            .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
            .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
            .build();
    return ResponseEntity.badRequest().body(response);
}

@ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse>handlerAppException(AppException exception){
    log.error(exception.getMessage());
    ApiResponse response = ApiResponse.builder()
            .code(exception.errorCode.getCode())
            .message(exception.getMessage()).build();
    return ResponseEntity.status(exception.errorCode.getHttpStatusCode()).body(response);
}

@ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse>handlerAccessDeniedException(AccessDeniedException exception){
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.builder()
                    .code(ErrorCode.UNAUTHORIZED.getCode())
                    .message(ErrorCode.UNAUTHORIZED.getMessage())
                    .build());
}

@ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse>handlerMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
    log.error(exception.getMessage());
    ApiResponse apiResponse = new ApiResponse();
    var enumKey = exception.getFieldError().getDefaultMessage();
    Map<String, Object> attributes = null;
    ErrorCode errorCode = ErrorCode.INVALID_KEY;
    try {
        errorCode = ErrorCode.valueOf(enumKey);
        // lấy dữ liệu chuyên sâu của lỗi đầu tiên
        var constraintViolation = exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
        // lấy metadata của lỗi do annotation cung cấp (min , max) và tổng hợp thành map
        attributes = constraintViolation.getConstraintDescriptor().getAttributes();
        log.warn(attributes.toString());
    } catch (IllegalArgumentException e) {
        log.error(e.getMessage());
    }
    apiResponse.setCode(errorCode.getCode());
    apiResponse.setMessage(Objects.nonNull(attributes) ?
            mapAttributes(errorCode.getMessage(), attributes)
            : errorCode.getMessage());
    return ResponseEntity.badRequest().body(apiResponse);

}
    private String mapAttributes(String message,Map<String, Object>attributes){
        String value = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}",value);
    }


}
