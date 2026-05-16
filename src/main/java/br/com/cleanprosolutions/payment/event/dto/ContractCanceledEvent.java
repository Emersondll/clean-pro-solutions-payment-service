package br.com.cleanprosolutions.payment.event.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event received when a contract is canceled with a refund policy applied.
 *
 * @param eventId        Unique event ID
 * @param contractId     The canceled contract ID
 * @param clientId       The client's ID (refund recipient)
 * @param amount         The original agreed amount
 * @param refundPercent  Percentage to refund: 100 (full), 50 (partial), or 0 (none)
 * @param timestamp      Event creation timestamp
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public record ContractCanceledEvent(
        String eventId,
        String contractId,
        String clientId,
        BigDecimal amount,
        int refundPercent,
        Instant timestamp
) {}
