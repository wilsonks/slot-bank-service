package com.slotcentral.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SlotBankServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlotBankServiceApplication.class, args);
    }
}
