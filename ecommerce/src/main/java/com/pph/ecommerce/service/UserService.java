package com.pph.ecommerce.service;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.request.UserUpdateRequest;
import com.pph.ecommerce.dto.response.PageResponse;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    String saveUser(UserCreationRequest request);
    UserResponse getUserToResponse(String userId);
    User getUser(String userId);
    String updateUser(String userId, UserUpdateRequest request);
    void deleteUser(String userId);
    void changeStatus(String userId, AccountStatus status);
    PageResponse<?> getAllUsersWithSortBySingleColumn(int offset, int limit, String sortBy);
    PageResponse<?> getAllUsersWithSortByMultiColumns(int offset, int limit, String[] sortBy);
    PageResponse<?> getAllUsersWithSortSingleColumnAndSearch(int offset, int limit, String search, String sortBy);
    PageResponse<?> advanceSearchWithCriteria(int offset, int limit, String address, String sortBy, String[] search);
    PageResponse<?> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] address);
}
