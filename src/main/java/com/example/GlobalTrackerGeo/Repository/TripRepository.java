package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Trip;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {
    // Láy all có sort, Hoặc dùng JPQL "query..."
    List<Trip> findByCustomerIdOrderByStatusAscCreatedAtAsc(Long customerId);
    // Láy phân trang có sort (customer)
    Page<Trip> findByCustomerId(Long customerId, Pageable pageable);
    // Lấy phân trang có sort (driver)
    Page<Trip> findByDriverId(Long driverId, Pageable pageable);

    Page<Trip> findByStatus(String status, PageRequest pageRequest);

    @Query("SELECT t.status FROM Trip t WHERE t.driverId = :driverId AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    List<String> findStatusesByDriverId(@Param("driverId") Long driverId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT t FROM Trip t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay")
    Page<Trip> findTripByCreatedAt(Pageable pageable, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Running Trips (status = "2", "3")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '2' OR t.status = '3'")
    long countRunningTrips();

    // Canceled Trips (status = "5")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '5'")
    long countCanceledTrips();
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '5' AND t.driverId = :driverId")
    long countCanceledTripsOfDriver(@Param("driverId") long driverId);
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '5' AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countCanceledTripsOfDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Completed Trips (status = "5")
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '4'")
    long countCompletedTrips();
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '4' AND t.driverId = :driverId")
    long countCompletedTripsOfDriver(Long driverId);
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status = '4' AND t.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countCompletedTripsOfDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Total Trips
    long count();
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driverId = :driverId")
    long countTripsOfDriver(long driverId);
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countTripsOfToday(LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Total Trips status != 1
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.status != '1'")
    long countTripsNotStatus1();

    // Lấy tất cả trips với trong khoảng thời gian 12 tháng
    @Query("SELECT t FROM Trip t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate ORDER BY t.createdAt DESC")
    List<Trip> findTripsByStatusAndDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Recent Ride
    List<Trip> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.customerId = :riderId")
    long countTripsByCustomerId(@Param("riderId") long riderId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driverId = :driverId")
    long countTripsByDriverId(@Param("driverId") long driverId);
}
