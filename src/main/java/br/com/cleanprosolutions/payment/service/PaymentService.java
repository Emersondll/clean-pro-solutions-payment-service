package br.com.cleanprosolutions.payment.service;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;

import java.util.List;

/**
 * Service contract for payment operations.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * Processes a webhook request from an external payment gateway.
     * If approved, publishes a PaymentApprovedEvent.
     *
     * @param request the webhook details
     */
    void processWebhook(PaymentWebhookRequest request);

    /**
     * Retrieves all payments for a specific contract.
     *
     * @param contractId the contract ID
     * @return the payment details
     */
    Payment findByContractId(String contractId);
}
