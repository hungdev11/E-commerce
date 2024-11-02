package com.pph.ecommerce.service.Imp;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.request.UserUpdateRequest;
import com.pph.ecommerce.dto.response.PageResponse;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.entity.Address;
import com.pph.ecommerce.entity.AddressStatus;
import com.pph.ecommerce.entity.User;
import com.pph.ecommerce.exception.AppException;
import com.pph.ecommerce.exception.ErrorCode;
import com.pph.ecommerce.mapper.AddressMapper;
import com.pph.ecommerce.mapper.UserMapper;
import com.pph.ecommerce.repository.SearchRepository;
import com.pph.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pph.ecommerce.utils.AppConst.SORT_BY;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService{
    UserRepository userRepository;
    AddressMapper addressMapper;
    UserMapper userMapper;
    SearchRepository searchRepository;
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
    public UserResponse getUserToResponse(String userId) {
        return userMapper.toUserResponse(userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    public String updateUser(String userId, UserUpdateRequest request) {
        User user = getUser(userId);
        userMapper.updateUser(user, request);

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

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
        User user = getUser(userId);
        if (user.getStatus() == AccountStatus.INACTIVE) return;
        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deleted successfully!");
    }

    @Override
    public void changeStatus(String userId, AccountStatus status) {
        User user = getUser(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Change status successfully");
    }

    @Override
    public PageResponse<?> getAllUsersWithSortBySingleColumn(int offset, int limit, String sortBy) {
        List<Sort.Order> orders = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                } else {
                    orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(orders));
        Page<User> users = userRepository.findAll(pageable);
        return convertToPageResponse(users, pageable);
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByMultiColumns(int offset, int limit, String[] sortBy) {
        List<Sort.Order> orders = new ArrayList<>();
        if (!Objects.isNull(sortBy)) {
            for (String sort : sortBy) {
                if (StringUtils.hasLength(sort)) {
                    Pattern pattern = Pattern.compile(SORT_BY);
                    Matcher matcher = pattern.matcher(sort);
                    if (matcher.find()) {
                        if (matcher.group(3).equalsIgnoreCase("desc")) {
                            orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                        } else {
                            orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                        }
                    }
                }
            }
        }
        Pageable pageable = PageRequest.of(offset, limit, Sort.by(orders));
        Page<User> users = userRepository.findAll(pageable);
        return convertToPageResponse(users, pageable);
    }

    @Override
    public PageResponse<?> getAllUsersWithSortSingleColumnAndSearch(int offset, int limit, String search, String sortBy) {
        return searchRepository.getAllUsersWithSortSingleColumnAndSearch(offset, limit, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchWithSpecifications(int offset, int limit, String address, String sortBy, String[] search) {
        return searchRepository.searchUserByCriteria(offset, limit, address, sortBy, search);
    }

    private PageResponse<?> convertToPageResponse(Page<User> users, Pageable pageable) {
        List<UserResponse> response = users.stream().map(userMapper::toUserResponse).toList();
        return PageResponse.builder()
                .offset(pageable.getPageNumber())
                .limit(pageable.getPageSize())
                .total(users.getTotalPages())
                .items(response)
                .build();
    }
    @Override
    public User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
