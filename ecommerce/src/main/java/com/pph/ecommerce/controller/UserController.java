package com.pph.ecommerce.controller;

import com.pph.ecommerce.dto.request.UserCreationRequest;
import com.pph.ecommerce.dto.request.UserUpdateRequest;
import com.pph.ecommerce.dto.response.ApiResponse;
import com.pph.ecommerce.dto.response.UserResponse;
import com.pph.ecommerce.entity.AccountStatus;
import com.pph.ecommerce.service.Imp.UserServiceImp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
                .code(HttpStatus.CREATED.value())
                .data(userService.saveUser(user))
                .message("Add user successfully!")
                .build();
    }
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable String userId) {
        log.info("Request get user, {}", userId);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .data(userService.getUserToResponse(userId))
                .message("Get user successfully!")
                .build();
    }
    @PutMapping("/{userId}")
    public ApiResponse<?> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        log.info("Request update user, {}", userId);
        return ApiResponse.<String>builder()
                .code(HttpStatus.ACCEPTED.value())
                .data(userService.updateUser(userId, request))
                .message("Update user successfully!")
                .build();
    }
    @DeleteMapping("/{userId}")
    public ApiResponse<?> deleteUser(@PathVariable String userId) {
        log.info("Request delete user, {}", userId);
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete user successfully!")
                .build();
    }
    @PatchMapping("/{userId}")
    public ApiResponse<?> changUserAccountStatus(@PathVariable String userId, @RequestParam AccountStatus status) {
        log.info("Request change account status of user, {}", userId);
        userService.changeStatus(userId, status);
        return ApiResponse.builder()
                .code(HttpStatus.ACCEPTED.value())
                .message("Modified status successfully")
                .build();
    }
    @GetMapping("/get-all-user-sort-by-single-column")
    public ApiResponse<?> getAllUsersWithSortBySingleColumn(@RequestParam(defaultValue = "0", required = false) int offset,
                                                            @RequestParam(defaultValue = "5", required = false) int limit,
                                                            @RequestParam(required = false) String sortBy) { //Pattern: firstname:asc|desc
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Users")
                .data(userService.getAllUsersWithSortBySingleColumn(offset, limit, sortBy))
                .build();
    }
    @GetMapping("/get-all-user-sort-by-multiple-columns")
    public ApiResponse<?> getAllUsersWithSortByMultiColumns(@RequestParam(defaultValue = "0", required = false) int offset,
                                                            @RequestParam(defaultValue = "5", required = false) int limit,
                                                            @RequestParam(required = false) String... sortBy) {
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Users")
                .data(userService.getAllUsersWithSortByMultiColumns(offset, limit, sortBy))
                .build();
    }
    @GetMapping("/get-all-user-sort-by-column-and-search") // Using customize query
    public ApiResponse<?> getAllUsersWithSortAndSearchSingleColumn(@RequestParam(defaultValue = "0", required = false) int offset,
                                                                   @RequestParam(defaultValue = "5", required = false) int limit,
                                                                   @RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) String sortBy) {
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Users")
                .data(userService.getAllUsersWithSortSingleColumnAndSearch(offset, limit, search, sortBy))
                .build();
    }
}
