package br.com.cleanprosolutions.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the Payment Service.
 *
 * <p>Handles payment orchestration, webhooks, and events
 * in the Clean Pro Solutions ecosystem.</p>
 *
 * @author Emerson Lima
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
