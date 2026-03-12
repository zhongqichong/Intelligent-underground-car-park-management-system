package com.garage.service;

import com.garage.dto.PaymentIntentResponse;
import com.garage.dto.StripeWebhookRequest;
import com.garage.repository.ParkingSessionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ParkingSessionRepository sessionRepository;

    @Value("${payment.stripe.secret-key}")
    private String stripeSecretKey;

    public PaymentIntentResponse createPaymentIntent(Long sessionId) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalArgumentException("Stripe secret key is not configured");
        }
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getFee() == null) {
            throw new IllegalArgumentException("Session fee is not ready");
        }
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(session.getFee().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                .setCurrency("cny")
                .putMetadata("sessionId", String.valueOf(sessionId))
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        session.setPaymentStatus("PAYMENT_INTENT_CREATED");
        session.setPaymentIntentId(intent.getId());
        sessionRepository.save(session);
        return new PaymentIntentResponse(intent.getClientSecret());
    }

    public void handleWebhook(StripeWebhookRequest request) {
        var session = sessionRepository.findByPaymentIntentId(request.paymentIntentId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment intent"));
        session.setPaymentStatus(request.status());
        sessionRepository.save(session);
    }
}
