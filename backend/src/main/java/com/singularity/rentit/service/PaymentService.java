package com.singularity.rentit.service;

import com.singularity.rentit.dto.response.PaymentIntentResponse;
import com.singularity.rentit.dto.response.PaymentSplitPreviewResponse;
import com.singularity.rentit.entity.Payment;
import com.singularity.rentit.entity.Rental;
import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.enums.PaymentStatus;
import com.singularity.rentit.enums.RentalStatus;
import com.singularity.rentit.exception.PaymentException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.PaymentRepository;
import com.singularity.rentit.repository.RentalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${stripe.platform-fee-percent}")
    private int platformFeePercent;

    private static final BigDecimal STRIPE_FEE_PERCENT = new BigDecimal("0.014");
    private static final BigDecimal STRIPE_FEE_FIXED = new BigDecimal("1.00");
    private static final BigDecimal GUARANTEE_RATE = new BigDecimal("0.50");

    @Transactional
    public PaymentIntentResponse createPaymentIntent(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        if (rental.getStatus() != RentalStatus.PENDING_PAYMENT) {
            throw new PaymentException("Rental is not awaiting payment");
        }

        BigDecimal total = rental.getTotalAmount();
        long amountInBani = total.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInBani)
                    .setCurrency("ron")
                    .putMetadata("rental_id", rentalId.toString())
                    .putMetadata("reference_number", rental.getReferenceNumber())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = Payment.builder()
                    .rental(rental)
                    .stripePaymentIntentId(intent.getId())
                    .amount(total)
                    .currency("RON")
                    .status(PaymentStatus.PENDING)
                    .guaranteeHeld(rental.getGuaranteeAmount())
                    .build();

            paymentRepository.save(payment);
            log.info("PaymentIntent created for rental {}: {}", rentalId, intent.getId());

            return new PaymentIntentResponse(
                    intent.getClientSecret(), intent.getId(), total, "RON", rentalId
            );
        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for intent " + paymentIntentId));

        Rental rental = payment.getRental();
        BigDecimal total = payment.getAmount();
        BigDecimal platformFee = total.multiply(BigDecimal.valueOf(platformFeePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal stripeFee = total.multiply(STRIPE_FEE_PERCENT).add(STRIPE_FEE_FIXED)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal ownerNet = total.subtract(platformFee);

        payment.setStatus(PaymentStatus.HELD);
        payment.setPlatformFee(platformFee);
        payment.setStripeFee(stripeFee);
        payment.setOwnerNetAmount(ownerNet);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        rental.setStatus(RentalStatus.PAID);
        rentalRepository.save(rental);

        emailService.sendRentalConfirmationToTenant(rental, payment);
        emailService.sendRentalNotificationToOwner(rental, payment);

        notificationService.send(
                rental.getTenant(),
                "Plată confirmată",
                "Plata pentru " + rental.getListing().getTitle() + " a fost procesată cu succes.",
                "PAYMENT_SUCCESS",
                rental.getId()
        );

        notificationService.send(
                rental.getOwner(),
                "Rezervare nouă",
                "Ai primit o rezervare pentru " + rental.getListing().getTitle(),
                "NEW_RENTAL",
                rental.getId()
        );

        log.info("Payment processed for rental {}", rental.getId());
    }

    @Transactional
    public void releaseGuarantee(Long rentalId) {
        Payment payment = paymentRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for rental " + rentalId));

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setGuaranteeReleasedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("Guarantee released for rental {}", rentalId);
    }

    public PaymentSplitPreviewResponse calculateSplit(BigDecimal pricePerDay, int days, CategoryType category) {
        BigDecimal subtotal = pricePerDay.multiply(BigDecimal.valueOf(days));
        BigDecimal guarantee = shouldApplyGuarantee(category)
                ? subtotal.multiply(GUARANTEE_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(guarantee);

        BigDecimal platformFee = subtotal.multiply(BigDecimal.valueOf(platformFeePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal stripeFee = total.multiply(STRIPE_FEE_PERCENT).add(STRIPE_FEE_FIXED)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal ownerReceives = subtotal.subtract(platformFee);

        return new PaymentSplitPreviewResponse(
                total, platformFee, stripeFee, ownerReceives, guarantee, days, pricePerDay
        );
    }

    public static boolean shouldApplyGuarantee(CategoryType category) {
        return category != CategoryType.REAL_ESTATE;
    }
}
