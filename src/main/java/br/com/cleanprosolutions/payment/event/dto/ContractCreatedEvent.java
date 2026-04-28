package br.com.cleanprosolutions.payment.event.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event received when a new contract is created.
 *
 * @param eventId      Unique event ID
 * @param contractId   The created contract ID
 * @param clientId     The client's ID
 * @param amount       The agreed amount to be paid
 * @param timestamp    Event creation timestamp
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public record ContractCreatedEvent(
        String eventId,
        String contractId,
        String clientId,
        BigDecimal amount,
        Instant timestamp
) {}
