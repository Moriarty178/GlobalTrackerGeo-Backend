package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.DriverLocationDTO;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Entity.DriverLocation;
import com.example.GlobalTrackerGeo.Repository.DriverLocationRepository;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Map;

public class DriverLocationService {

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    @Qualifier("redisTemplateDriverLocation")
    private RedisTemplate<String, Object> redisTemplateDriverLocation;

    @Transactional
    public void saveLocation(DriverLocationDTO location) {
        //tạo 1 đối tượng DriverLocation mới với thông tin vị trí hiện tại
        Driver driver = driverRepository.findById(location.getDriverId()).orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriver(driver);
        driverLocation.setLatitude(location.getLatitude());
        driverLocation.setLongitude(location.getLongitude());
        driverLocation.setSpeed(40.0);//tạm thời set default = 40km/h
        driverLocation.setTimestamp(location.getTimestamp());
        driverLocation.setRecordedAt(LocalDateTime.now());

        //Lưu vị trí hiện tại vào PostgreSQL để sau này làm phần history
        driverLocationRepository.save(driverLocation);

        //Lưu vị trí hiện tại vào Redis Stream để phân tích và cảnh báo sau này
        redisTemplateDriverLocation.opsForStream().add("driver_location_stream", Map.of("driverId", driver.getDriverId().toString(), "locationData", location));

    }
}
