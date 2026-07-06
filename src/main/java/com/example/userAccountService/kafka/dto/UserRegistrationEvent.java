package com.example.userAccountService.kafka.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationEvent {

    private String email;
    private String firstName;
    private String lastName;
    private String accountNumber;

    @Builder.Default
    private String bankName = "NOVA BANK";

}
