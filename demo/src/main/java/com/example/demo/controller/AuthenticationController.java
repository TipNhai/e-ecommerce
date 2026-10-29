package com.example.demo.controller;


import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    UserService userService;
    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@RequestBody @Valid RegisterRequest request){
        return ApiResponse.<UserProfileResponse>builder().result(userService.register(request)).build();
    }
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request){
        return ApiResponse.<AuthenticationResponse>builder().result(authenticationService.login(request)).build();
    }

    @PostMapping("/refreshToken")
    public ApiResponse<AuthenticationResponse>refreshToken(@RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }
    @PostMapping("/logout")
    public ApiResponse<String>logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<String>builder()
                .result("Logout successfully")
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse>introspect(@RequestBody IntrospectRequest request){
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }
}
