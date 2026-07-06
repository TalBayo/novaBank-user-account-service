package com.example.userAccountService.service.impl;

import com.example.userAccountService.dto.*;
import com.example.userAccountService.entity.Account;
import com.example.userAccountService.entity.User;
import com.example.userAccountService.exceptions.BadRequestException;
import com.example.userAccountService.exceptions.NotFoundException;
import com.example.userAccountService.repository.AccountRepository;
import com.example.userAccountService.repository.UserRepository;
import com.example.userAccountService.service.UserService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<UserWithAccountDTO> getCurrentUserDetails() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Fetching current user details for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Account not found for user id: " + user.getId()));

        UserWithAccountDTO userWithAccountDTO = mapToUserWithAccount(user, account);


        return new ApiResponse<>(HttpStatus.OK.value(),
                "profile retrieved",
                userWithAccountDTO);
    }

    @Override
    public ApiResponse<UserWithAccountDTO> searchUser(String email, String accountNumber) {
        log.info("Searching user by email={} accountNumber={}", email, accountNumber);

        User user;
        Account account;

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasAccount = accountNumber != null && !accountNumber.isBlank();

        // 1. validation
        if (!hasEmail && !hasAccount) {
            throw new BadRequestException("Email or account number is required");
        }

        if (hasEmail && hasAccount) {
            throw new BadRequestException("Provide either email or account number, not both");
        }

        // 2. business logic
        if (hasEmail) {
            log.debug("Searching user by email: {}", email);
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

            account = accountRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Account not found for user"));

        } else {
            log.debug("Searching user by accountNumber: {}", accountNumber);
            account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new NotFoundException("Account not found with number: " + accountNumber));

            user = account.getUser();
        }

        UserWithAccountDTO userWithAccountDTO = mapToUserWithAccount(user, account);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "profile retrieved successfully",
                userWithAccountDTO);
    }

    @Override
    public ApiResponse<Page<UserDTO>> getAllUsers(String roleName, Pageable pageable) {
        log.info("Getting all users with roleName={}", roleName);

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isUnsorted()
                        ? Sort.by("createdAt").descending()
                        : pageable.getSort()
        );

        Page<User> userPage;
        if (roleName != null && !roleName.isBlank()){
            userPage = userRepository.findByRoleName(roleName.toUpperCase(), sortedPageable);
        }else{
            userPage = userRepository.findAll(sortedPageable);
        }

        Page<UserDTO> dtoPage = userPage.map(user -> modelMapper.map(user, UserDTO.class));
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "users fetched successfully",
                dtoPage
        );
    }

    @Override
    public ApiResponse<UserStatisticsDTO> getUserStatistics() {

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long totalAccounts = accountRepository.count();

        double avgAccountsPerUser = totalUsers == 0
                ? 0
                : (double) totalAccounts / totalUsers;

        UserStatisticsDTO stats = UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .totalAccounts(totalAccounts)
                .averageAccountPerUser(avgAccountsPerUser)
                .customersCount(userRepository.countByRoleName("CUSTOMER"))
                .adminsCount(userRepository.countByRoleName("ADMIN"))
                .build();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "statistics fetched successfully",
                stats
        );
    }

    @Override
    public ApiResponse<String> updateUserStatus(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("User not found with id: " + userId));

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String status = user.isEnabled() ? "Enabled" : "Disabled";
        log.info("User status updated to {} for userId={}", status, userId);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "User Status Changed to: " + status,
                null
        );
    }

    private UserWithAccountDTO mapToUserWithAccount(User user, Account account) {
        return UserWithAccountDTO.builder()
                .user(modelMapper.map(user, UserDTO.class))
                .account(modelMapper.map(account, AccountDTO.class))
                .build();
    }

}
