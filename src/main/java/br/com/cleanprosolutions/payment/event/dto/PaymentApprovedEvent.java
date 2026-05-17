package br.com.cleanprosolutions.payment.event.dto;

import java.time.Instant;

/**
 * Event published when a payment is approved.
 *
 * <p>Consumable by the {@code contract-service} to confirm the contract,
 * and by {@code notification-service} to alert the client.</p>
 *
 * @param eventId      Unique event ID
 * @param paymentId    The payment transaction ID
 * @param contractId   The associated contract ID
 * @param timestamp    Event creation timestamp
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public record PaymentApprovedEvent(
        String eventId,
        String paymentId,
        String contractId,
        Instant timestamp
) {}
