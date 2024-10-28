package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();
        // Lấy thông tin tương ứng 6 card trên dashboard
        stats.put("totalRiders", customerRepository.count());// Total customer
        stats.put("totalDrivers", driverRepository.count());// Total driver
        stats.put("vehicleType", vehicleRepository.count());// Total vehicle
        stats.put("revenue", paymentRepository.calculateTotalRevenue());// Total revenue
        stats.put("totalRides", tripRepository.count());// Total Trips
        stats.put("canceledRides", tripRepository.countCanceledTrips());// Canceled Trips
        stats.put("runningRides", tripRepository.countRunningTrips());//  Running Trips (state = "2", "3")
        stats.put("completedRides", tripRepository.countCompletedTrips());// Completed Trips (state = "4")
        return stats;
    }
}
