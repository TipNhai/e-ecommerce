package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/auth/refreshToken"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
     http
             //tắt csrf
             .csrf(AbstractHttpConfigurer::disable)
             //bật cors cho front-end
             .cors(cors->cors.configurationSource(corsConfigurationSource()))
             //cấu hình xác thực
             .authorizeHttpRequests(auth->auth
//                     .requestMatchers(HttpMethod.POST,"/**").permitAll()
//                     .requestMatchers("api/admin").hasAuthority("ADMIN")
                             .requestMatchers(
                                     "/api/v1/payments/callback/**",
                                     "/api/v1/payments/ipn/**"
                             ).permitAll()
                             .requestMatchers(HttpMethod.GET, "/api/v1/files/view/**").permitAll()
                             .requestMatchers(HttpMethod.POST, "/api/v1/files/**").authenticated()
                             // Các API khởi tạo / xem đơn vẫn phải xác thực
                             .requestMatchers("/api/v1/payments/**").authenticated()
                     .anyRequest().permitAll()
             )
             .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

     return http.build();
 }
    // Cấu hình CORS chi tiết cho phép Frontend gọi API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); // URL Frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
}