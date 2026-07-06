package com.example.userAccountService.repository;

import com.example.userAccountService.entity.Account;
import com.example.userAccountService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUser(User user);

    boolean existsByAccountNumber(String accountNumber);

}
