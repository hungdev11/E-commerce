package com.pph.ecommerce.mapper;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "addresses", ignore = true) // Avoid duplicate address with user_id null and non null
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
