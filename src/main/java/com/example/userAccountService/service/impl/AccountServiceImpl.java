package com.example.userAccountService.service.impl;

import com.example.userAccountService.dto.AccountDTO;
import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.entity.Account;
import com.example.userAccountService.entity.User;
import com.example.userAccountService.enums.AccountStatus;
import com.example.userAccountService.exceptions.NotFoundException;
import com.example.userAccountService.repository.AccountRepository;
import com.example.userAccountService.repository.UserRepository;
import com.example.userAccountService.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    public ApiResponse<AccountDTO> getMyAccount() {

        String userEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        log.info("Fetching account for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Account Not Found"));


        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);

        accountDTO.setOwnerEmail(account.getUser().getEmail());

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account Retrieved",
                accountDTO
        );
    }

    @Override
    public ApiResponse<AccountDTO> getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Not Found"));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);

        accountDTO.setOwnerEmail(account.getUser().getEmail());

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account Retrieved",
                accountDTO
        );
    }

    @Override
    public ApiResponse<AccountDTO> changeAccountStatus(String accountNumber, AccountStatus status) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Not Found"));

        account.setAccountStatus(status);
        Account savedAccount = accountRepository.save(account);


        AccountDTO accountDTO = modelMapper.map(savedAccount, AccountDTO.class);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account Status Updated Successfully",
                accountDTO
        );
    }

    @Override
    public ApiResponse<Page<AccountDTO>> getAllAccounts(Pageable pageable) {

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by("createdAt").descending();


        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Account> accounts = accountRepository.findAll(sortedPageable);

        Page<AccountDTO> dtoPage = accounts.map(account -> modelMapper.map(account, AccountDTO.class));

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Accounts Retrieved",
                dtoPage
        );
    }
}
