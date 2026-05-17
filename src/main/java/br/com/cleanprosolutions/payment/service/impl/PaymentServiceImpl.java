package br.com.cleanprosolutions.payment.service.impl;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.dto.PaymentResponse;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.PaymentApprovedEvent;
import br.com.cleanprosolutions.payment.event.dto.PaymentFailedEvent;
import br.com.cleanprosolutions.payment.exception.PaymentNotFoundException;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import br.com.cleanprosolutions.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of {@link PaymentService}.
 *
 * <p>Processes payment webhooks from the external gateway. On success, publishes
 * {@code PaymentApprovedEvent}. On failure, publishes {@code PaymentFailedEvent}
 * for SAGA rollback (contract cancellation).</p>
 *
 * <p>Idempotency: webhooks for already-terminal payments are silently ignored.</p>
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.payment:payment.exchange}")
    private String paymentExchange;

    @Value("${rabbitmq.routing-key.payment-approved:payment.approved}")
    private String paymentApprovedRoutingKey;

    @Value("${rabbitmq.routing-key.payment-failed:payment.failed}")
    private String paymentFailedRoutingKey;

    @Override
    public void processWebhook(final PaymentWebhookRequest request) {
        log.info("Processing webhook — contractId: {}, externalTransactionId: {}",
                request.contractId(), request.externalTransactionId());

        final Payment payment = repository.findByContractId(request.contractId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No pending payment found for contractId: " + request.contractId()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment for contractId {} already in status {}. Ignoring webhook.",
                    request.contractId(), payment.getStatus());
            return;
        }

        payment.setExternalTransactionId(request.externalTransactionId());
        payment.setUpdatedAt(Instant.now());

        if (Boolean.TRUE.equals(request.success())) {
            payment.setStatus(PaymentStatus.APPROVED);
            repository.save(payment);
            publishPaymentApprovedEvent(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            repository.save(payment);
            publishPaymentFailedEvent(payment, request.failureReason());
        }
    }

    @Override
    public PaymentResponse findByContractId(final String contractId) {
        log.info("Fetching payment for contractId: {}", contractId);
        final Payment payment = repository.findByContractId(contractId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for contractId: " + contractId));
        return toResponse(payment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void publishPaymentApprovedEvent(final Payment payment) {
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                UUID.randomUUID().toString(),
                payment.getId(),
                payment.getContractId(),
                Instant.now()
        );
        log.info("Publishing PaymentApprovedEvent — paymentId: {}", payment.getId());
        rabbitTemplate.convertAndSend(paymentExchange, paymentApprovedRoutingKey, event);
    }

    private void publishPaymentFailedEvent(final Payment payment, final String reason) {
        final PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID().toString(),
                payment.getContractId(),
                reason != null ? reason : "Payment rejected by gateway",
                Instant.now()
        );
        log.warn("Publishing PaymentFailedEvent — contractId: {}, reason: {}",
                payment.getContractId(), event.reason());
        rabbitTemplate.convertAndSend(paymentExchange, paymentFailedRoutingKey, event);
    }

    private PaymentResponse toResponse(final Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getContractId(),
                payment.getClientId(),
                payment.getAmount(),
                payment.getExternalTransactionId(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
