package com.example.demo.dto.response;
import java.io.Serial;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// Response DTO cho toàn bộ danh sách Favorite của User
public class FavoriteResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    Long id;
    String userId;
    List<ProductSummaryResponse> products;
}


