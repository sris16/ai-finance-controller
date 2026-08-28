package com.razorpay.aifinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiFinanceControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiFinanceControllerApplication.class, args);
    }
}
