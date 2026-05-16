package br.com.cleanprosolutions.payment.service;

import br.com.cleanprosolutions.payment.dto.PaymentResponse;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;

/**
 * Service contract for payment operations.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * Processes a webhook from an external payment gateway.
     * Publishes {@code PaymentApprovedEvent} on success or
     * {@code PaymentFailedEvent} on failure (SAGA rollback).
     *
     * @param request the webhook payload
     */
    void processWebhook(PaymentWebhookRequest request);

    /**
     * Returns the payment associated with a contract.
     *
     * @param contractId the contract ID
     * @return payment response DTO
     */
    PaymentResponse findByContractId(String contractId);
}
