package com.pph.ecommerce.service.Imp;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.Address;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService{
    UserRepository userRepository;
    AddressMapper addressMapper;
    UserMapper userMapper;
    @Override
    public String saveUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        request.getAddresses().forEach(a -> user.saveAddress(addressMapper.toAddress(a)));
        userRepository.save(user);
        log.info("User has save!");
        return user.getId();
    }

    @Override
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND)));
    }
}
