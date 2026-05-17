package br.com.cleanprosolutions.payment.service.impl;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.dto.PaymentResponse;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.PaymentApprovedEvent;
import br.com.cleanprosolutions.payment.event.dto.PaymentFailedEvent;
import br.com.cleanprosolutions.payment.exception.PaymentNotFoundException;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PaymentServiceImpl}.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentServiceImpl service;

    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "paymentExchange", "payment.exchange");
        ReflectionTestUtils.setField(service, "paymentApprovedRoutingKey", "payment.approved");
        ReflectionTestUtils.setField(service, "paymentFailedRoutingKey", "payment.failed");

        pendingPayment = new Payment("pay-1", "contract-1", "client-1",
                new BigDecimal("150.00"), PaymentStatus.PENDING);
        pendingPayment.setCreatedAt(Instant.now());
        pendingPayment.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("shouldApprovePaymentAndPublishApprovedEvent")
    void shouldApprovePaymentAndPublishApprovedEvent() {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "contract-1", true, null
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(pendingPayment));
        when(repository.save(any(Payment.class))).thenReturn(pendingPayment);

        service.processWebhook(request);

        final ArgumentCaptor<Payment> savedCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.APPROVED);

        verify(rabbitTemplate).convertAndSend(
                eq("payment.exchange"), eq("payment.approved"), any(PaymentApprovedEvent.class)
        );
    }

    @Test
    @DisplayName("shouldFailPaymentAndPublishFailedEvent")
    void shouldFailPaymentAndPublishFailedEvent() {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "contract-1", false, "INSUFFICIENT_FUNDS"
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(pendingPayment));
        when(repository.save(any(Payment.class))).thenReturn(pendingPayment);

        service.processWebhook(request);

        final ArgumentCaptor<Payment> savedCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);

        final ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("payment.exchange"), eq("payment.failed"), eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().reason()).isEqualTo("INSUFFICIENT_FUNDS");
    }

    @Test
    @DisplayName("shouldUseDefaultReasonWhenFailureReasonIsNull")
    void shouldUseDefaultReasonWhenFailureReasonIsNull() {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "contract-1", false, null
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(pendingPayment));
        when(repository.save(any(Payment.class))).thenReturn(pendingPayment);

        service.processWebhook(request);

        final ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("payment.exchange"), eq("payment.failed"), eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().reason()).isEqualTo("Payment rejected by gateway");
    }

    @Test
    @DisplayName("shouldIgnoreWebhookWhenPaymentIsAlreadyTerminal")
    void shouldIgnoreWebhookWhenPaymentIsAlreadyTerminal() {
        pendingPayment.setStatus(PaymentStatus.APPROVED);
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "contract-1", true, null
        );
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(pendingPayment));

        service.processWebhook(request);

        verify(repository, org.mockito.Mockito.never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("shouldThrowWhenContractNotFoundDuringWebhook")
    void shouldThrowWhenContractNotFoundDuringWebhook() {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "missing", true, null
        );
        when(repository.findByContractId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processWebhook(request))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("shouldReturnPaymentResponseByContractId")
    void shouldReturnPaymentResponseByContractId() {
        when(repository.findByContractId("contract-1")).thenReturn(Optional.of(pendingPayment));

        final PaymentResponse response = service.findByContractId("contract-1");

        assertThat(response).isNotNull();
        assertThat(response.contractId()).isEqualTo("contract-1");
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("shouldThrowWhenPaymentNotFoundByContractId")
    void shouldThrowWhenPaymentNotFoundByContractId() {
        when(repository.findByContractId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByContractId("missing"))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
