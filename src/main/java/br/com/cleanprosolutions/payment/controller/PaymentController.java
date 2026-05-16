package br.com.cleanprosolutions.payment.controller;

import br.com.cleanprosolutions.payment.dto.PaymentResponse;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Payments", description = "Payment webhook receiver and payment status queries")
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/webhook")
    @Operation(summary = "Receive payment webhook from external gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody final PaymentWebhookRequest request) {
        log.info("POST /payments/webhook — contractId: {}", request.contractId());
        service.processWebhook(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Get payment status for a given contract")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getByContractId(@PathVariable final String contractId) {
        log.info("GET /payments/contract/{}", contractId);
        return ResponseEntity.ok(service.findByContractId(contractId));
    }
}
