package br.com.cleanprosolutions.payment.event.consumer;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.ContractCanceledEvent;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ContractCanceledConsumer}.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ContractCanceledConsumerTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private ContractCanceledConsumer consumer;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment("pay-1", "contract-1", "client-1",
                new BigDecimal("150.00"), PaymentStatus.APPROVED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("shouldCancelPaymentWithFullRefundWhenRefundIs100Percent")
    void shouldCancelPaymentWithFullRefundWhenRefundIs100Percent() {
        final ContractCanceledEvent event = new ContractCanceledEvent(
                "event-1", "contract-1", "client-1", new BigDecimal("150.00"), 100, Instant.now()
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenReturn(payment);

        consumer.handleContractCanceled(event);

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("shouldCancelPaymentWithPartialRefundWhenRefundIs50Percent")
    void shouldCancelPaymentWithPartialRefundWhenRefundIs50Percent() {
        final ContractCanceledEvent event = new ContractCanceledEvent(
                "event-1", "contract-1", "client-1", new BigDecimal("150.00"), 50, Instant.now()
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenReturn(payment);

        consumer.handleContractCanceled(event);

        verify(repository).save(any(Payment.class));
    }

    @Test
    @DisplayName("shouldCancelPaymentWithNoRefundWhenRefundIsZero")
    void shouldCancelPaymentWithNoRefundWhenRefundIsZero() {
        final ContractCanceledEvent event = new ContractCanceledEvent(
                "event-1", "contract-1", "client-1", new BigDecimal("150.00"), 0, Instant.now()
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenReturn(payment);

        consumer.handleContractCanceled(event);

        final ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("shouldSkipWhenNoPaymentFoundForContract")
    void shouldSkipWhenNoPaymentFoundForContract() {
        final ContractCanceledEvent event = new ContractCanceledEvent(
                "event-1", "missing", "client-1", new BigDecimal("150.00"), 100, Instant.now()
        );
        when(repository.findByContractId("missing")).thenReturn(Optional.empty());

        consumer.handleContractCanceled(event);

        verify(repository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("shouldHandleNullAmountWithZeroRefund")
    void shouldHandleNullAmountWithZeroRefund() {
        final ContractCanceledEvent event = new ContractCanceledEvent(
                "event-1", "contract-1", "client-1", null, 100, Instant.now()
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(payment));
        when(repository.save(any(Payment.class))).thenReturn(payment);

        consumer.handleContractCanceled(event);

        verify(repository).save(any(Payment.class));
    }
}
