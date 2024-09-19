package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {
    // Láy all có sort, Hoặc dùng jpql "query..."
    List<Trip> findByCustomerIdOrderByStatusAscCreatedAtAsc(Long customerId);
    // Láy phân trang có sort (customer)
    Page<Trip> findByCustomerId(Long customerId, Pageable pageable);
    // Lấy phân trang có sort (driver)
    Page<Trip> findByDriverId(Long driverId, Pageable pageable);
}
