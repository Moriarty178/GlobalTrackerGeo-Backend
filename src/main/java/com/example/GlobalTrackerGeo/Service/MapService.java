package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.DriverLocationDTO;
import com.example.GlobalTrackerGeo.Entity.Map;
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
        Map driverInTheMap = mapRepository.findByDriverId(location.getDriverId());
        // Nếu driverID tồn tại => cập nhật location
        if (driverInTheMap != null) {
            driverInTheMap.setLatitude(location.getLatitude());
            driverInTheMap.setLongitude(location.getLongitude());
            mapRepository.save(driverInTheMap);
        } else {  // Nếu driverID ko có -> tạo mới
            Map addNewDriverToMap = new Map(location.getDriverId(), location.getLatitude(), location.getLongitude());
            mapRepository.save(addNewDriverToMap);
        }
    }

    @Transactional
    public void removeDriverFromMap(long driverId) {
        mapRepository.deleteByDriverId(driverId);
    }

    public List<Map> getAllDriverActive() {
        return mapRepository.findAll();
    }
}
