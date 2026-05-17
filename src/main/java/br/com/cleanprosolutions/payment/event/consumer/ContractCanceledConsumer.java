package br.com.cleanprosolutions.payment.event.consumer;

import br.com.cleanprosolutions.payment.document.Payment;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.event.dto.ContractCanceledEvent;
import br.com.cleanprosolutions.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

/**
 * Consumer for {@code ContractCanceled} events.
 *
 * <p>Applies the refund policy to the existing payment record.
 * If refundPercent is 0 the payment is canceled with no refund;
 * otherwise the refund amount is logged and the payment marked as CANCELED.</p>
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractCanceledConsumer {

    private final PaymentRepository repository;

    @RabbitListener(queues = "${rabbitmq.queue.contract-canceled:contract.canceled.payment.queue}")
    @Transactional
    public void handleContractCanceled(final ContractCanceledEvent event) {
        log.info("Received ContractCanceledEvent — contractId: {}, refundPercent: {}%",
                event.contractId(), event.refundPercent());

        final Optional<Payment> existing = repository.findByContractId(event.contractId());

        if (existing.isEmpty()) {
            log.warn("No payment found for contractId: {} — skipping refund processing.", event.contractId());
            return;
        }

        final Payment payment = existing.get();
        final BigDecimal refundAmount = computeRefundAmount(event.amount(), event.refundPercent());

        log.info("Processing refund for contractId: {} — amount: {}, refundPercent: {}%, refundAmount: {}",
                event.contractId(), event.amount(), event.refundPercent(), refundAmount);

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setUpdatedAt(Instant.now());
        repository.save(payment);

        log.info("Payment for contractId: {} marked as CANCELED with refund of {}", event.contractId(), refundAmount);
    }

    private BigDecimal computeRefundAmount(final BigDecimal amount, final int refundPercent) {
        if (refundPercent == 0 || amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
