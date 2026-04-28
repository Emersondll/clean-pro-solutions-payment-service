package br.com.cleanprosolutions.payment.service.impl;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.PaymentApprovedEvent;
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
 * @author Clean Pro Solutions Team
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

    @Override
    public void processWebhook(final PaymentWebhookRequest request) {
        log.info("Processing webhook for contractId: {}, externalTransactionId: {}",
                request.contractId(), request.externalTransactionId());

        final Payment payment = repository.findByContractId(request.contractId())
                .orElseThrow(() -> new PaymentNotFoundException("No pending payment found for contractId: " + request.contractId()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment for contractId {} is already in status {}. Ignoring webhook.",
                    request.contractId(), payment.getStatus());
            return;
        }

        payment.setExternalTransactionId(request.externalTransactionId());

        if (Boolean.TRUE.equals(request.success())) {
            payment.setStatus(PaymentStatus.APPROVED);
            publishPaymentApprovedEvent(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            // TODO: Publicar evento PaymentFailedEvent para rollback na saga (CancelContract)
            log.warn("Payment failed for contractId: {}", request.contractId());
        }

        payment.setUpdatedAt(Instant.now());
        repository.save(payment);
    }

    @Override
    public Payment findByContractId(final String contractId) {
        log.info("Fetching payment details for contractId: {}", contractId);
        return repository.findByContractId(contractId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for contractId: " + contractId));
    }

    private void publishPaymentApprovedEvent(final Payment payment) {
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                UUID.randomUUID().toString(),
                payment.getId(),
                payment.getContractId(),
                Instant.now()
        );

        log.info("Publishing PaymentApprovedEvent for paymentId: {}", payment.getId());
        rabbitTemplate.convertAndSend(paymentExchange, paymentApprovedRoutingKey, event);
    }
}
