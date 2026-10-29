package com.example.demo.mapper;

import com.example.demo.dto.request.CreateAddressRequest;
import com.example.demo.dto.request.UpdateAddressRequest;
import com.example.demo.dto.response.AddressResponse;
import com.example.demo.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "ward" , source = "ward")
    Address toEntity(CreateAddressRequest request);

    AddressResponse toResponse(Address address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRequest(UpdateAddressRequest request, @MappingTarget Address address);

}
