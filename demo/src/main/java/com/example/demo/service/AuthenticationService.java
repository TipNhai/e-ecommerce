package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.LogoutRequest;
import com.example.demo.dto.request.RefreshTokenRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;

import static com.example.demo.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider jwtTokenProvider;
    RedisTokenService redisTokenService;
    public AuthenticationResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(()->
                new AppException(UNAUTHENTICATED));
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())) 
            throw new AppException(UNAUTHORIZED);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        redisTokenService.saveRefreshToken(user.getId(),refreshToken);
        log.info(redisTokenService.getRefreshToken(user.getId()));
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    //Khi token hết hạn tự động xin refresh
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        var userId = validateAndRevokeActiveSession(request.getRefreshToken(), request.getAccessToken());

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        redisTokenService.saveRefreshToken(userId,newRefreshToken);
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // Thu hồi access token cũ và xóa refresh token trong redis
        String userId = validateAndRevokeActiveSession(request.getRefreshToken(), request.getAccessToken());
        redisTokenService.deleteToken(userId);
        log.info("User [{}] đã đăng xuất thành công, đã thu hồi session.", userId);
    }

    //dùng cho fe kiểm tra token
    public IntrospectResponse introspect(IntrospectRequest request){
        var isValid = true;
        try{
            var signedJWT = jwtTokenProvider.verifyRefreshToken(request.getToken());
            var saveRefreshToken = redisTokenService.getRefreshToken(signedJWT.getJWTClaimsSet().getSubject());
            if(saveRefreshToken == null || !request.getToken().equals(saveRefreshToken)) {
                isValid = false;
            }
        } catch (JOSEException | ParseException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    //Các hàm helper
    /**
     * Xác thực Refresh Token, kiểm tra Whitelist, dọn dẹp Access Token cũ và trả về User.
     */
    private String validateAndRevokeActiveSession(String refreshToken, String accessToken) throws ParseException, JOSEException {
        // Chuẩn hóa: Cắt bỏ "Bearer " nếu client truyền kèm vào refreshToken
        if (StringUtils.hasText(refreshToken) && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7).trim();
        }

        // 1. Verify Refresh Token (Chữ ký & Hạn dùng)
        var signedJWT = jwtTokenProvider.verifyRefreshToken(refreshToken);
        var userId = signedJWT.getJWTClaimsSet().getSubject();

        // 2. Check Whitelist Redis
        var saveRefreshToken = redisTokenService.getRefreshToken(userId);
        if (saveRefreshToken == null) {
            log.error("Refresh thất bại: Không tìm thấy Refresh Token trong Redis cho User [{}] (Session đã logout hoặc hết hạn)", userId);
            throw new AppException(UNAUTHORIZED);
        }
        if (!saveRefreshToken.equals(refreshToken)) {
            log.error("Refresh thất bại: Token gửi lên KHÔNG KHỚP với Token đang lưu trong Redis cho User [{}] (Cơ chế Token Rotation: token này có thể đã được sử dụng trước đó)", userId);
            throw new AppException(UNAUTHORIZED);
        }
        // 3. Thu hồi Access Token cũ nếu vẫn còn hạn
        revokeAccessTokenIfActive(accessToken);

        return userId;
    }

    // Tách nhỏ tiếp hàm revoke Access Token cho sạch code
    private void revokeAccessTokenIfActive(String accessToken) {
        if (StringUtils.hasText(accessToken)) {
            // Chuẩn hóa: Cắt bỏ tiền tố "Bearer " nếu client truyền kèm
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7).trim();
            }
            try {
                var signedAccess = jwtTokenProvider.verifyAccessToken(accessToken);
                long remainingMillis = signedAccess.getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();
                long remainingSeconds = remainingMillis / 1000;

                if (remainingSeconds > 0) {
                    redisTokenService.blacklistToken(accessToken, remainingSeconds);
                    log.info("Đã đưa Access Token vào Blacklist với TTL: {} giây", remainingSeconds);
                }
            } catch (Exception e) {
                log.info("Access Token cũ đã hết hạn hoặc không hợp lệ, không cần blacklist: {}", e.getMessage());
            }
        }
    }
}