package com.pph.ecommerce.mapper;

import com.pph.ecommerce.dto.request.AddressRequest;
import com.pph.ecommerce.entity.Address;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toAddress(AddressRequest request) {
        if ( request == null ) {
            return null;
        }

        Address.AddressBuilder address = Address.builder();

        address.houseNumber( request.getHouseNumber() );
        address.street( request.getStreet() );
        address.city( request.getCity() );
        address.country( request.getCountry() );
        address.state( request.getState() );

        return address.build();
    }
}
