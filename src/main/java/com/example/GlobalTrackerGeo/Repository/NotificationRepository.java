package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
}
