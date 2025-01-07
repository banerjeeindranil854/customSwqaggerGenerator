package com.mtn.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.mtn.aggregator", "com.mtn.madapi"})
public class TransferService {
    public static void main(String[] args) {
        SpringApplication.run(TransferService.class, args);
    }
}
