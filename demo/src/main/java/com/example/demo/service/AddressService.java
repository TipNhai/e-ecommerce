package com.example.demo.service;

import com.example.demo.dto.request.CreateAddressRequest;
import com.example.demo.dto.request.UpdateAddressRequest;
import com.example.demo.dto.response.AddressResponse;
import com.example.demo.entity.Address;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressService {
    AddressRepository addressRepository;
    AddressMapper addressMapper;
    UserRepository userRepository;
    @Transactional
    @CacheEvict(value = "address", key = "#userId")
    public AddressResponse createAddress(String userId, CreateAddressRequest request){
        var user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        var address = addressMapper.toEntity(request);
        address.setUser(user);
        // Nếu danh sách địa chỉ trống hoặc request yêu cầu set mặc định
        boolean hasNoAddress = addressRepository.countByUserId(userId) == 0;
        if (hasNoAddress || Boolean.TRUE.equals(request.getIsDefault())) {
            unsetOtherDefaults(userId);
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    @CacheEvict(value = "address", key = "#userId")
    public AddressResponse updateAddress(String userId, Long addressId, UpdateAddressRequest request){
        Address address = addressRepository.findByIdAndUserId(addressId, userId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        addressMapper.updateAddressFromRequest(request, address);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetOtherDefaults(userId);
            address.setIsDefault(true);
        }
        return addressMapper.toResponse(address);
    }

    @Transactional
    @Cacheable(value = "address" , key = "#userId" , sync = true)
    public List<AddressResponse>getMyAddress(String userId){
        return addressRepository.findAllByUserId(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "address", key = "#userId")
    public void deleteAddress(String userId, Long addressId){
        Address address = addressRepository.findByIdAndUserId(addressId, userId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        addressRepository.delete(address);
    }

    //Helper
    private void unsetOtherDefaults(String userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(addr ->
                    addr.setIsDefault(false)
                );
    }

}
