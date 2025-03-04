package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.PaymentRequest;
import com.example.GlobalTrackerGeo.Entity.Payment;
import com.example.GlobalTrackerGeo.Repository.PaymentRepository;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public void savePayment(PaymentRequest paymentRequest, String tripId) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setTripId(tripId);
        payment.setPrice(paymentRequest.getPrice());
        payment.setVoucher(paymentRequest.getVoucher());
        payment.setTotal(paymentRequest.getTotal());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus());

        paymentRepository.save(payment);
    }

    public void setStatus(String tripId) {
        Optional<Payment> optionalPayment = paymentRepository.findByTripId(tripId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setPaymentStatus("Paid");

            paymentRepository.save(payment);
        }
    }
}
