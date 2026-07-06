package com.example.userAccountService.controller;


import com.example.userAccountService.dto.AccountDTO;
import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.enums.AccountStatus;
import com.example.userAccountService.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> listAllAccounts(
            @PageableDefault(page = 0, size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<AccountDTO>> changeAccountStatus(
            @RequestParam String accountNumber,
            @RequestParam AccountStatus status
    ) {
        return ResponseEntity.ok(accountService.changeAccountStatus(accountNumber, status));
    }
}
