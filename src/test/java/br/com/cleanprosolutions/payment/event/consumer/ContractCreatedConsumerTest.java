package br.com.cleanprosolutions.payment.event.consumer;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.event.dto.ContractCreatedEvent;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ContractCreatedConsumer}.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ContractCreatedConsumerTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private ContractCreatedConsumer consumer;

    private ContractCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new ContractCreatedEvent(
                "event-1", "contract-1", "client-1", new BigDecimal("150.00"), Instant.now()
        );
    }

    @Test
    @DisplayName("shouldCreatePendingPaymentWhenContractCreated")
    void shouldCreatePendingPaymentWhenContractCreated() {
        when(repository.findByContractId(event.contractId())).thenReturn(Optional.empty());

        consumer.handleContractCreated(event);

        verify(repository).save(any(Payment.class));
    }

    @Test
    @DisplayName("shouldIgnoreEventWhenPaymentAlreadyExists")
    void shouldIgnoreEventWhenPaymentAlreadyExists() {
        when(repository.findByContractId(event.contractId())).thenReturn(Optional.of(new Payment()));

        consumer.handleContractCreated(event);

        verify(repository, never()).save(any(Payment.class));
    }
}
