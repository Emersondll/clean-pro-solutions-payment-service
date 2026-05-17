package br.com.cleanprosolutions.payment.dto;

import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for payment queries.
 *
 * <p>Exposes only necessary fields — shields the internal {@code Payment}
 * document and the {@code @Version} field from API consumers.</p>
 *
 * @param id                    payment ID
 * @param contractId            associated contract ID
 * @param clientId              payer's user ID
 * @param amount                payment amount
 * @param externalTransactionId gateway transaction reference
 * @param status                current payment status
 * @param createdAt             creation timestamp
 * @param updatedAt             last update timestamp
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public record PaymentResponse(
        String id,
        String contractId,
        String clientId,
        BigDecimal amount,
        String externalTransactionId,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
