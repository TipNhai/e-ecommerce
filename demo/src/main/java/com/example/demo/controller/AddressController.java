package com.example.demo.controller;

import com.example.demo.dto.request.CreateAddressRequest;
import com.example.demo.dto.request.UpdateAddressRequest;
import com.example.demo.dto.response.AddressResponse;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.AddressService;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/addresses")
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequiredArgsConstructor
public class AddressController {
    AddressService addressService;

    @PostMapping("/{userId}")
    public ApiResponse<AddressResponse> createAddress(@Valid @PathVariable String userId, @RequestBody CreateAddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.createAddress(userId,request))
                .build();
    }

    @PutMapping("/{userId}/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable String userId, @PathVariable Long addressId ,@Valid @RequestBody UpdateAddressRequest request){
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.updateAddress(userId, addressId, request))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<AddressResponse>> getMyAddress(@PathVariable String userId){
        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getMyAddress(userId))
                .build();
    }

    @DeleteMapping("/{userId}/{addressId}")
    public ApiResponse<String> deleteAddress(@PathVariable String userId, @PathVariable Long addressId){
        addressService.deleteAddress(userId,addressId);
        return ApiResponse.<String>builder()
                .result("Xóa địa chỉ thành công")
                .build();
    }

}
