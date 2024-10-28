package com.pph.ecommerce.dto.request;

import com.pph.ecommerce.entity.AccountStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstname;
    String lastname;
    String email;
    String phoneNumber;
    Set<AddressRequest> addresses;
    AccountStatus status;
    LocalDate dateOfBirth;
}
