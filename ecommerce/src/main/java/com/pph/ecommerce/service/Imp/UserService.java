package com.pph.ecommerce.service.Imp;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.request.UserUpdateRequest;
import com.pph.ecommerce.dto.response.UserResponse;

public interface UserService {
    String saveUser(UserCreationRequest request);
    UserResponse getUser(String userId);
    String updateUser(String userId, UserUpdateRequest request);
    void deleteUser(String userId);
}
