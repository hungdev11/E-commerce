package com.pph.ecommerce.dto.response;

import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.entity.Address;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String password;
    String firstname;
    String lastname;
    String email;
    String phoneNumber;
    LocalDate dateOfBirth;
}
