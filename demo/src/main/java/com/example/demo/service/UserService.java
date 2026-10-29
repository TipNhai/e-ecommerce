package com.example.demo.service;

import com.example.demo.config.BloomFilterManager;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.request.UpdateProfileRequest;
import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.event.UserRegistrationEvent;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static com.example.demo.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    AddressMapper addressMapper;
    PasswordEncoder passwordEncoder;
    BloomFilterManager bloomFilterManager;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        // sau phải xử lý exception
        if(!request.getPassword().equals(request.getConfirmPassword()))
            throw new AppException(PASSWORD_NOT_MATCH);
        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(USER_EXISTED);
        User user = userMapper.registerToEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try{
            userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            throw new AppException(USER_EXISTED);
        }
        bloomFilterManager.addUser(user.getId());

        // Bắn sự kiện qua Kafka để Consumer xử lý gửi Welcome Email (sau khi DB đã COMMIT)
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            UserRegistrationEvent event = UserRegistrationEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        kafkaTemplate.send("user_registration_topic", user.getId(), event);
                        log.info("Đã bắn sự kiện UserRegistrationEvent lên Kafka sau khi Transaction COMMIT: {}", user.getId());
                    }
                });
            } else {
                kafkaTemplate.send("user_registration_topic", user.getId(), event);
            }
        }

        return userMapper.toProfileResponse(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public UserProfileResponse updateProfile(String id, UpdateProfileRequest request){
        User user = userRepository.findById(id).orElseThrow(()->new AppException(USER_NOT_FOUND));
        //Nên xử lý thêm phần address để sửa và thêm mới riêng biệt
        var addressNew = addressMapper.toEntity(request.getAddress());
        addressNew.setUser(user);
        user.getAddresses().add(addressNew);
        userMapper.updateToEntity(request,user);
        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }
    public UserProfileResponse getProfile(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        // Principal chính là User entity đã được load từ JwtAuthenticationFilter
        User user = (User) authentication.getPrincipal();
        String userId = user.getId();
        
        return userMapper.toProfileResponse(userRepository.findById(userId).orElseThrow(()
                ->new AppException(USER_NOT_FOUND)));
    }
    public List<UserResponse> getAllUser(){
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }
    @Cacheable(value = "users", key = "#id", sync = true)
    public UserResponse getUserById(String id){
        // 1. CHẶN ĐỨNG CACHE PENETRATION TẠI ĐÂY
        if (!bloomFilterManager.mightContainUser(id)) {
            // Chắc chắn không tồn tại, văng lỗi ngay không cần chạm DB
            throw new AppException(USER_NOT_FOUND);
        }
        // 2. Đi tiếp vào DB (nếu lọt qua Bloom Filter)
        return userMapper.toResponse(userRepository.findById(id)
                .orElseThrow(()->new AppException(USER_NOT_FOUND)));
    }


}
