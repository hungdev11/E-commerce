package com.pph.ecommerce.dto.request;

import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.entity.Address;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String username;
    String password;
    String firstname;
    String lastname;
    String email;
    String phoneNumber;
    Set<AddressRequest> addresses;
    AccountStatus status;
    LocalDate dateOfBirth;
}
