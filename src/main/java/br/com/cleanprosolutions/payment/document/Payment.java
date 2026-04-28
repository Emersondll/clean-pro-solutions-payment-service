package br.com.cleanprosolutions.payment.document;

import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document representing a payment transaction.
 *
 * <p>Uses optimistic locking via {@code @Version} to handle concurrent
 * webhook updates and prevent race conditions.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String contractId;
    private String clientId;

    private BigDecimal amount;

    private String externalTransactionId;

    private PaymentStatus status;

    @Version
    private Long version;

    private Instant createdAt;
    private Instant updatedAt;

    public Payment() {
    }

    public Payment(final String id, final String contractId, final String clientId,
                   final BigDecimal amount, final PaymentStatus status) {
        this.id = id;
        this.contractId = contractId;
        this.clientId = clientId;
        this.amount = amount;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }

    public String getContractId() { return contractId; }
    public void setContractId(final String contractId) { this.contractId = contractId; }

    public String getClientId() { return clientId; }
    public void setClientId(final String clientId) { this.clientId = clientId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(final BigDecimal amount) { this.amount = amount; }

    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(final String externalTransactionId) { this.externalTransactionId = externalTransactionId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(final PaymentStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(final Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final Instant updatedAt) { this.updatedAt = updatedAt; }
}
