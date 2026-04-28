package br.com.cleanprosolutions.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a payment transaction is not found.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(final String message) {
        super(message);
    }
}
