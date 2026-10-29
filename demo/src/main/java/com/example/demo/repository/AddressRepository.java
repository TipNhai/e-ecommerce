package com.example.demo.repository;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> user(User user);

    Optional<Address> findByIdAndUserId(Long id, String userId);

    int countByUserId(String userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);

    Optional<Address> findByUserId(String userId);

    List<Address> findAllByUserId(String userId);
}
