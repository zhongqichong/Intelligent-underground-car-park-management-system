package com.garage.service;

import com.garage.repository.ParkingSessionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentServiceTest {

    @Test
    void shouldFailWhenStripeSecretMissing() {
        ParkingSessionRepository repository = Mockito.mock(ParkingSessionRepository.class);
        PaymentService service = new PaymentService(repository);
        ReflectionTestUtils.setField(service, "stripeSecretKey", "");

        assertThrows(IllegalArgumentException.class, () -> service.createPaymentIntent(1L));
    }
}
