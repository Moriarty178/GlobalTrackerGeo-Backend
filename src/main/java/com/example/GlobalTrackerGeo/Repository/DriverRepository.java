package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Driver findByEmail(String email);

    // Total drivers
    long count();
    @Query("SELECT COUNT(d) FROM Driver d WHERE d.status = 'Pending'")
    long countDriversUnapproved();

//    @Query("SELECT d FROM Driver d WHERE d.status = 'Pending' ORDER BY createdAt DESC")
    Page<Driver> findByStatus(String status, Pageable pageable);

}
