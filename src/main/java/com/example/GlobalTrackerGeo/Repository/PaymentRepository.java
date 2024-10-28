package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Payment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByTripId(String tripId);

    // Total revenue
    @Query("SELECT SUM(p.total) FROM Payment p WHERE p.paymentStatus = 'Paid'")
    Double calculateTotalRevenue();
    @Query("SELECT SUM(p.total) FROM Payment p JOIN Trip t ON p.tripId = t.tripId WHERE p.paymentStatus = 'Paid' AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    Double calculateTotalRevenueOfToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT SUM(p.total) FROM Payment p JOIN Trip t ON p.tripId = t.tripId WHERE p.paymentStatus = 'Paid' AND t.driverId = :driverId")
    Double calculateRevenueOfDriver(long driverId);
}
