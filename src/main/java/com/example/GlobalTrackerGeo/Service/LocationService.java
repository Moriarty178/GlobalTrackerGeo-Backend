package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Entity.DriverLocation;
import com.example.GlobalTrackerGeo.Repository.DriverLocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class LocationService {

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private AdminWebService adminWebService;

    @Autowired
    @Qualifier("redisTemplateDriverLocation")
    private RedisTemplate<String, Object> redisTemplateDriverLocation;

    @Transactional
    public void updateDriverLocation(Driver driver, double latitude, double longitude, double speed) {
        //tạo 1 đối tượng DriverLocation mới với thông tin vị trí hiện tại
        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriver(driver);
        driverLocation.setLatitude(latitude);
        driverLocation.setLongitude(longitude);
        driverLocation.setSpeed(speed);
        driverLocation.setRecordedAt(LocalDateTime.now());

        //Lưu vị trí hiện tại vào PostgreSQL để sau này làm phần history
        driverLocationRepository.save(driverLocation);

        //Gửi thông tin vị trí đến Admin Web
        adminWebService.sendToAdminWeb(driverLocation);

        //Lưu vị trí hiện tại vào Redis Stream để phân tích và cảnh báo sau này
        redisTemplateDriverLocation.opsForStream().add("driver_location_stream", Map.of("driverId", driver.getDriverId().toString(), "locationData", driverLocation));

    }

}
