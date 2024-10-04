package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Trip;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {
    // Láy all có sort, Hoặc dùng jpql "query..."
    List<Trip> findByCustomerIdOrderByStatusAscCreatedAtAsc(Long customerId);
    // Láy phân trang có sort (customer)
    Page<Trip> findByCustomerId(Long customerId, Pageable pageable);
    // Lấy phân trang có sort (driver)
    Page<Trip> findByDriverId(Long driverId, Pageable pageable);

    Page<Trip> findByStatus(String status, PageRequest pageRequest);

    @Query("SELECT t.status FROM Trip t WHERE t.driverId = :driverId AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    List<String> findStatusesByDriverId(@Param("driverId") Long driverId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Running Trips (status = "2", "3")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '2' OR t.status = '3'")
    long countRunningTrips();
    // Canceled Trips (status = "5")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '5'")
    long countCanceledTrips();
    // Completed Trips (status = "5")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '4'")
    long countCompletedTrips();
    // Total Trips
    long count();
}
