package com.example.demo.event;

import com.example.demo.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditEvent {
    private String actorId;     // Ai thực hiện? (Admin/User ID)
    private AuditAction action;      // Làm gì? (Ví dụ: "UPDATE_PRODUCT_PRICE")
    private String entityName;  // Tác động lên cái gì? ("PRODUCT")
    private String entityId;    // ID của sản phẩm đó
    private String details;     // Chuỗi JSON lưu giá cũ và giá mới
}
