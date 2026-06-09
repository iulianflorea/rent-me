package com.singularity.rentit.repository;

import com.singularity.rentit.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRentalId(Long rentalId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
