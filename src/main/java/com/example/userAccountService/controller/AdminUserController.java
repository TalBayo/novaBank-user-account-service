package com.example.userAccountService.controller;

import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.dto.UserDTO;
import com.example.userAccountService.dto.UserStatisticsDTO;
import com.example.userAccountService.dto.UserWithAccountDTO;
import com.example.userAccountService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserWithAccountDTO>> search(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String accountNumber
    ) {
        log.info("Admin search request - email: {}, accountNumber: {}", email, accountNumber);

        return ResponseEntity.ok(userService.searchUser(email, accountNumber));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String roleName,
            @PageableDefault(page = 0, size = 50) Pageable pageable
    ) {
        log.info("Fetching users - role: {}, page: {}, size: {}",
                roleName, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(userService.getAllUsers(roleName, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatisticsDTO>> getStatistics() {
        log.info("Fetching user statistics");

        return ResponseEntity.ok(userService.getUserStatistics());
    }

    @PatchMapping("/toggle-status/{userId}")
    public ResponseEntity<ApiResponse<String>> toggleStatus(@PathVariable Long userId) {
        log.info("Toggling status for userId: {}", userId);

        return ResponseEntity.ok(userService.updateUserStatus(userId));
    }
}
