package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
