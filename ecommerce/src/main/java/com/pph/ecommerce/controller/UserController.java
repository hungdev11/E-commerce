package com.pph.ecommerce.controller;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.response.ApiResponse;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.service.Imp.UserServiceImp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserServiceImp userService;
    @PostMapping("/")
    public ApiResponse<String> createUser(@RequestBody UserCreationRequest user) {
        log.info("Request add user, {} {}", user.getFirstname(), user.getLastname());
        return ApiResponse.<String>builder()
                .data(userService.saveUser(user))
                .build();
    }
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable String userId) {
        log.info("Request get user, {}", userId);
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUser(userId))
                .build();
    }

}
