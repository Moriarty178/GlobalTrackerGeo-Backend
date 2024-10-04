package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.MapDriver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MapRepository extends JpaRepository<MapDriver, Long> {
    MapDriver findByDriverId(Long driverId);

    void deleteByDriverId(Long driverId);
}
