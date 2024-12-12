package com.coffee.sweepstakes.controller;

import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.model.request.UserRequest;
import com.coffee.sweepstakes.service.EmailService;
import com.coffee.sweepstakes.service.UserService;
import com.coffee.sweepstakes.model.response.ResponseDto;
import com.coffee.sweepstakes.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseDto<User> saveUser(@RequestBody UserRequest userRequest) {
        log.info("received request for user registration");
        return userService.saveUser(userRequest);
    }

    @GetMapping("/{userId}")
    public ResponseDto<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("getting user details based on this id, {}", userId);
        return userService.getById(userId);
    }

    @GetMapping
    public ResponseDto<UserResponse> getAllUser(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                @RequestParam(name = "search", required = false) String search,
                                                @RequestParam(required = false) String eventCode) {
        log.info("getting all users");
        return userService.getAllUsers(pageable, search, eventCode);
    }

    @GetMapping("/update-user-status/{userId}")
    @Operation(summary = "Update the status of user")
    public ResponseEntity<String> updateUserStatus(@PathVariable UUID userId, @RequestParam Boolean isActive) {
        log.info("Received request to update the status of user for userID : {}", userId);
        userService.updateIsActiveById(userId, isActive);
        return ResponseEntity.ok("Updated successfully");
    }

    @GetMapping("/downloadCSV")
    public void downloadCSVFile(HttpServletResponse response,
                                @RequestParam(name = "eventCode") String eventCode) {
        log.info("getting all users for csv generation");
        userService.downloadCSVFile(null, eventCode, response);
    }

    @PostMapping("/resend/{userId}")
    public ResponseEntity<String> resendEmail(@PathVariable UUID userId) {
        log.info("Received request to resend email for userId: {}", userId);
        emailService.resendEmail(userId);
        return ResponseEntity.ok("Email re-sent successfully");
    }

}
