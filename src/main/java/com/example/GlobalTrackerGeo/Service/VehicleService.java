package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.Vehicle;
import com.example.GlobalTrackerGeo.Repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public void saveVehicle(String name, Double cost, String status, String imagePath) {
        // save -> db
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);
        vehicle.setCost(cost);
        vehicle.setStatus(status);
        vehicle.setImg(imagePath);

        vehicleRepository.save(vehicle);
    }
}
