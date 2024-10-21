package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.PromoCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
}
