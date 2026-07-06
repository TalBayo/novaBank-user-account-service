package com.example.userAccountService.kafka.service;


import com.example.userAccountService.kafka.dto.BalanceUpdateEvent;
import com.example.userAccountService.kafka.dto.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    //TOPICS
    private static final String USER_TOPIC = "user-registered-events";
    private static final String BALANCE_UPDATE_TOPIC = "balance-update-notification-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishedUserRegistrationEvent(UserRegistrationEvent event) {
        try {
            kafkaTemplate.send(USER_TOPIC, event.getEmail(), event);
            log.info("Event Sent Out {}", event.getEmail());

        } catch (Exception e) {
            log.error("Failed to publish user registered event: {}", e.getMessage());
        }
    }

    public void publishTransactionNotificationEvent(BalanceUpdateEvent event) {
        try {
            kafkaTemplate.send(BALANCE_UPDATE_TOPIC, event.getEmail(), event);
            log.info("Balance Update Event Sent Out {}", event.getEmail());

        } catch (Exception e) {
            log.info("Failed to Balance Update event: {}", e.getMessage());
        }
    }

}
