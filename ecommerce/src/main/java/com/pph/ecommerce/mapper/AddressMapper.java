package com.pph.ecommerce.mapper;

import com.pph.ecommerce.dto.request.AddressRequest;
import com.pph.ecommerce.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressRequest request);
}
