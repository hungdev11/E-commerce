package com.pph.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user")
public class User extends AbstractEntity {
    String username;
    String password;
    String firstname;
    String lastname;
    String email;
    @Column(name = "phone_number")
    String phoneNumber;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    Set<Address> addresses;
    public void saveAddress(Address address) {
        if (Objects.isNull(address)) return;
        if (Objects.isNull(addresses)) {
            addresses = new HashSet<>();
        }
        addresses.add(address);
        address.setUser(this);
    }
    @Enumerated(EnumType.STRING)
    AccountStatus status;
    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;
}
