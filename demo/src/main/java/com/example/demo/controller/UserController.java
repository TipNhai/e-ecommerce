package com.example.demo.controller;

import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PutMapping("/update/{userId}")
    public ApiResponse<UserProfileResponse>updateProfile(@PathVariable String userId, @RequestBody @Valid UpdateProfileRequest request){
        return ApiResponse.<UserProfileResponse>builder().result(userService.updateProfile(userId,request)).build();
    }

    @GetMapping("/getMyProfile")
    public ApiResponse<UserProfileResponse>getMyProfile() {
        return ApiResponse.<UserProfileResponse>builder().result(userService.getProfile()).build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>>getAllUser(){
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUser())
                .build();
    }

    @GetMapping("/getUserById/{userId}")
    public ApiResponse<UserResponse>getUserById(@PathVariable String userId){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }
}
