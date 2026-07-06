package com.example.userAccountService.service;

import com.example.userAccountService.dto.AccountDTO;
import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    ApiResponse<AccountDTO> getMyAccount();

    ApiResponse<AccountDTO> getAccountByAccountNumber(String accountNumber);

    ApiResponse<AccountDTO> changeAccountStatus(String accountNumber, AccountStatus status);

    ApiResponse<Page<AccountDTO>> getAllAccounts(Pageable pageable);
}
