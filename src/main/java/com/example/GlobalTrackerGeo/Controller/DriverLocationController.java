package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import com.example.GlobalTrackerGeo.Service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
public class DriverLocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private DriverRepository driverRepository;

    @PostMapping("/update-location")//nhận dữ liệu vị trí của tài xế từ FE post lên
    public ResponseEntity<?> updateDriverLocation(
            @RequestParam Long driverId,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double speed) {

        //Lấy đối tượng tài xế từ csdl
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(()-> new IllegalArgumentException("Driver not found"));
        //cập nhật vị trí của tài xế
        locationService.updateDriverLocation(driver, latitude, longitude, speed);

        return ResponseEntity.ok("Driver location updated successfully");
    }
}
