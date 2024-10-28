package com.pph.ecommerce.service.Imp;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.request.UserUpdateRequest;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.entity.Address;
import com.pph.ecommerce.entity.AddressStatus;
import com.pph.ecommerce.entity.User;
import com.pph.ecommerce.exception.AppException;
import com.pph.ecommerce.exception.ErrorCode;
import com.pph.ecommerce.mapper.AddressMapper;
import com.pph.ecommerce.mapper.UserMapper;
import com.pph.ecommerce.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService{
    UserRepository userRepository;
    AddressMapper addressMapper;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public String saveUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXITED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        request.getAddresses().forEach(a -> user.saveAddress(addressMapper.toAddress(a)));
        userRepository.save(user);
        log.info("User has save!");
        return user.getId();
    }

    @Override
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    public String updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Xử lý các địa chỉ
        Set<Address> addressSet = new HashSet<>();
        if (!request.getAddresses().isEmpty()) {
            user.getAddresses().forEach(a -> a.setStatus(AddressStatus.INACTIVE));
            request.getAddresses().forEach(a -> {
                Address address = addressMapper.toAddress(a);
                address.setStatus(AddressStatus.ACTIVE);
                addressSet.add(address);
                user.saveAddress(address);
            });
            user.setAddresses(addressSet);
        }
        userRepository.save(user);
        log.info("User updated successfully!");
        return user.getId();
    }

    @Override
    public void deleteUser(String userId) {
        //userRepository.deleteById(userId);
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == AccountStatus.INACTIVE) return;
        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deleted successfully!");
    }

}
