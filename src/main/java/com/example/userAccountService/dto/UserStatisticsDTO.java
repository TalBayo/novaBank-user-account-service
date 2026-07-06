package com.example.userAccountService.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDTO {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long totalAccounts;
    private double averageAccountPerUser;
    private long customersCount;
    private long adminsCount;

}
