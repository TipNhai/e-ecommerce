package com.example.demo.config;

import com.example.demo.exception.AppException;
import com.example.demo.service.RedisTokenService;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;



@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token)) {
                // 1. CHỈ VERIFY CHỮ KÝ & HẠN DÙNG (Không gọi Redis nữa!)
                SignedJWT signedJWT = jwtTokenProvider.verifyAccessToken(token);
                String userId = signedJWT.getJWTClaimsSet().getSubject();

                //số lượng blacklist nhỏ , sẽ thu hồi trong thời gian ngắn nên không nghẽn cổ chai như whitelist
                if (redisTokenService.isTokenBlacklist(token)) {
                    log.warn("Access Token đã bị vô hiệu hóa (Blacklisted): {}", token);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                // 2. Load UserDetails & Set Authentication vào Context
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Xác thực thất bại: {}", ex.getMessage());
            // KHÔNG THROW EXCEPTION Ở ĐÂY!
            // Chỉ cần xóa Context -> Spring Security sẽ tự biết User này chưa xác thực
            SecurityContextHolder.clearContext();
        }

        //FilterChain luôn ngoài try/catch
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
