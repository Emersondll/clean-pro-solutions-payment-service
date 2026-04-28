package br.com.cleanprosolutions.payment.controller;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment operations.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment Webhook and queries")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/webhook")
    @Operation(summary = "Receive payment webhook from external gateway")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody final PaymentWebhookRequest request) {
        log.info("POST /payments/webhook");
        service.processWebhook(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get payment details by contract ID")
    public ResponseEntity<Payment> getByContractId(@PathVariable final String contractId) {
        log.info("GET /payments/contract/{}", contractId);
        return ResponseEntity.ok(service.findByContractId(contractId));
    }
}
