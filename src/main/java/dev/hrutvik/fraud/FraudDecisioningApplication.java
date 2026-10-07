package dev.hrutvik.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FraudDecisioningApplication {
    public static void main(String[] args) { SpringApplication.run(FraudDecisioningApplication.class, args); }
}
