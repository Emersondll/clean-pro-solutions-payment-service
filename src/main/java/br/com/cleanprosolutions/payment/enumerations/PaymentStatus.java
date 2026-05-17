package br.com.cleanprosolutions.payment.enumerations;

/**
 * Status of a payment transaction.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public enum PaymentStatus {
    /** Payment requested, waiting for gateway callback. */
    PENDING,

    /** Payment authorized and approved by the gateway. */
    APPROVED,

    /** Payment declined or failed. */
    FAILED,

    /** Payment canceled or refunded. */
    CANCELED
}
