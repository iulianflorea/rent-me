package com.singularity.rentit.controller;

import com.singularity.rentit.dto.response.PaymentIntentResponse;
import com.singularity.rentit.dto.response.PaymentSplitPreviewResponse;
import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/intent/{rentalId}")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@PathVariable Long rentalId) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(rentalId));
    }

    @GetMapping("/preview")
    public ResponseEntity<PaymentSplitPreviewResponse> getPreview(
            @RequestParam BigDecimal pricePerDay,
            @RequestParam int days,
            @RequestParam CategoryType category
    ) {
        return ResponseEntity.ok(paymentService.calculateSplit(pricePerDay, days, category));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload, sigHeader, webhookSecret
            );

            if ("payment_intent.succeeded".equals(event.getType())) {
                event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                    com.stripe.model.PaymentIntent intent = (com.stripe.model.PaymentIntent) obj;
                    paymentService.handlePaymentSuccess(intent.getId());
                });
            }

            return ResponseEntity.ok("OK");
        } catch (com.stripe.exception.SignatureVerificationException e) {
            log.warn("Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error");
        }
    }
}
