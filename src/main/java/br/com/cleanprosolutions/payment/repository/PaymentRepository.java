package br.com.cleanprosolutions.payment.repository;

import br.com.cleanprosolutions.payment.document.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Payment}.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Finds a payment by its associated contract ID.
     *
     * @param contractId the contract ID
     * @return optional payment
     */
    Optional<Payment> findByContractId(String contractId);
}
