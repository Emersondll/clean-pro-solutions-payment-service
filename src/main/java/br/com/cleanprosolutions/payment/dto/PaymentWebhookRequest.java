package br.com.cleanprosolutions.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO representing a webhook payload from an external payment gateway.
 *
 * @param externalTransactionId ID from Stripe, MercadoPago, etc.
 * @param contractId            internal contract ID for association
 * @param success               whether the payment was approved
 * @param failureReason         human-readable failure reason (null when success is true)
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public record PaymentWebhookRequest(

        @NotBlank(message = "External transaction ID is required")
        String externalTransactionId,

        @NotBlank(message = "Contract ID is required")
        String contractId,

        @NotNull(message = "Success status is required")
        Boolean success,

        String failureReason
) {}
