package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.DriverLocationDTO;
import com.example.GlobalTrackerGeo.Entity.MapDriver;
import com.example.GlobalTrackerGeo.Repository.MapRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapService {

    @Autowired
    private MapRepository mapRepository;

    @Transactional
    public void saveOrUpdateDriverLocationToMap(DriverLocationDTO location) {
        // Kiểm tra 'Map'
        MapDriver driverInTheMapDriver = mapRepository.findByDriverId(location.getDriverId());
        // Nếu driverID tồn tại => cập nhật location
        if (driverInTheMapDriver != null) {
            driverInTheMapDriver.setLatitude(location.getLatitude());
            driverInTheMapDriver.setLongitude(location.getLongitude());
            mapRepository.save(driverInTheMapDriver);
        } else {  // Nếu driverID ko có -> tạo mới
            MapDriver addNewDriverToMapDriver = new MapDriver(location.getDriverId(), location.getLatitude(), location.getLongitude());
            mapRepository.save(addNewDriverToMapDriver);
        }
    }

    @Transactional
    public void removeDriverFromMap(long driverId) {
        mapRepository.deleteByDriverId(driverId);
    }

    public List<MapDriver> getAllDriverActive() {
        return mapRepository.findAll();
    }
}
