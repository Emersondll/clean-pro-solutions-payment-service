package br.com.cleanprosolutions.payment.event.dto;

import java.time.Instant;

/**
 * Event published when a payment fails.
 *
 * <p>Consumed by the Contract Service to roll back the contract status
 * to CANCELED, completing the compensating transaction in the SAGA.</p>
 *
 * @param eventId    unique event identifier (idempotency key)
 * @param contractId the contract associated with the failed payment
 * @param reason     human-readable failure reason from the gateway
 * @param timestamp  event creation time
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public record PaymentFailedEvent(
        String eventId,
        String contractId,
        String reason,
        Instant timestamp
) {}
