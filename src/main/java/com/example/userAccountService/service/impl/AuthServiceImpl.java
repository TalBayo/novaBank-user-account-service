package com.example.userAccountService.service.impl;


import com.example.userAccountService.dto.*;
import com.example.userAccountService.entity.Account;
import com.example.userAccountService.entity.Role;
import com.example.userAccountService.entity.User;
import com.example.userAccountService.enums.AccountStatus;
import com.example.userAccountService.enums.AccountType;
import com.example.userAccountService.enums.Currency;
import com.example.userAccountService.exceptions.BadRequestException;
import com.example.userAccountService.exceptions.NotFoundException;
import com.example.userAccountService.kafka.dto.UserRegistrationEvent;
import com.example.userAccountService.kafka.service.AccountEventPublisher;
import com.example.userAccountService.repository.AccountRepository;
import com.example.userAccountService.repository.RoleRepository;
import com.example.userAccountService.repository.UserRepository;
import com.example.userAccountService.security.JwtService;
import com.example.userAccountService.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AccountEventPublisher accountEventPublisher;

    @Override
    public ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest) {

        log.info("We are inside the register user service method");

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Account already exist for this email");
        }

        Set<Role> roles = new HashSet<>();

        String roleName = (registrationRequest.getRole() != null && !registrationRequest.getRole().isBlank())
                ? registrationRequest.getRole().toUpperCase()
                : "CUSTOMER";

        Role databaseRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role with name " + roleName + " Not found"));

        roles.add(databaseRole);

        User userToSave = User.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(userToSave);

        //generate a unique account number for the user
        String accountNumber = generateUniqueAccountNumber();

        Account accountToSaveToDb = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(Currency.USD)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .user(savedUser)
                .build();

        accountRepository.save(accountToSaveToDb);

        //Publish event out to the notification service
        UserRegistrationEvent userRegistrationEvent = UserRegistrationEvent.builder()
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .accountNumber(accountNumber)
                .bankName("NOVA BANK")
                .build();

        accountEventPublisher.publishedUserRegistrationEvent(userRegistrationEvent);

        //convert to dto
        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        AuthResponse authResponse = AuthResponse.builder()
                .user(userDTO)
                .build();

        return new ApiResponse<>(HttpStatus.CONTINUE.value(), "User account created successfully", authResponse);

    }

    @Override
    public ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest) {

        log.info("Login attempt for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BadRequestException("User is disabled");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String token = jwtService.generateToken(user.getEmail(), roles);

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "login successful",
                authResponse
        );
    }

    private String generateUniqueAccountNumber() {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        String accountNumber;

        do {
            int randomPart = random.nextInt(100_000_000);
            accountNumber = String.format("%08d", randomPart);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
