package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Entity.Payment;
import com.example.GlobalTrackerGeo.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable String tripId) {
        Optional<Payment> paymentOptional = paymentRepository.findByTripId(tripId);
        if (paymentOptional.isPresent()) {
            return ResponseEntity.ok(paymentOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
