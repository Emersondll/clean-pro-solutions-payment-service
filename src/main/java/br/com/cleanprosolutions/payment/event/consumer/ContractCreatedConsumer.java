package br.com.cleanprosolutions.payment.event.consumer;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.ContractCreatedEvent;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumer for {@code ContractCreated} events.
 *
 * <p>Initializes a pending payment record when a contract is created.</p>
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractCreatedConsumer {

    private final PaymentRepository repository;

    @RabbitListener(queues = "${rabbitmq.queue.contract-created:contract.created.queue}")
    @Transactional
    public void handleContractCreated(final ContractCreatedEvent event) {
        log.info("Received ContractCreatedEvent for contractId: {}", event.contractId());

        if (repository.findByContractId(event.contractId()).isPresent()) {
            log.warn("Payment already exists for contractId: {}. Ignoring event for idempotency.", event.contractId());
            return;
        }

        final Payment payment = new Payment(
                UUID.randomUUID().toString(),
                event.contractId(),
                event.clientId(),
                event.amount(),
                PaymentStatus.PENDING
        );
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        repository.save(payment);
        log.info("Pending payment created for contractId: {}", event.contractId());
    }
}
