package com.pph.ecommerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    String houseNumber;
    String street;
    String city;
    String country;
    String state;
}
