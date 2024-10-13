package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DriverService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DriverRepository driverRepository;

    public List<DriverDTO> findNearestDrivers (Location locSource) {
        // Truy vấn tất cả tài xế đang hoạt động được lưu trong 'map'
        String sql = "SELECT m.driver_id, d.first_name, m.latitude, m.longitude " +
                     "FROM map m " +
                     "JOIN drivers d ON m.driver_id = d.driver_id";

        List<DriverDTO> allDrivers = jdbcTemplate.query(sql, (rs, rowNum) -> {
            long driverId = rs.getLong("driver_id");
            String driverName = rs.getString("first_name");
            double latitude = rs.getDouble("latitude");
            double longitude = rs.getDouble("longitude");

            Location driverLocation = new Location(latitude, longitude, "");
            double distance = calculateDistance(locSource, driverLocation);

            return new DriverDTO(driverId, driverName, driverLocation, distance);
        });

        // Tìm tối đa 7 tái xế gần nhất dựa trên khoảng cách
        return allDrivers.stream()
                .sorted(Comparator.comparingDouble(DriverDTO::getDistance))
                .limit(7)
                .collect(Collectors.toList());
    }

    private double calculateDistance(Location locSource, Location driverLocation) {
        // Công thức Haversine tính khoảng cách giữa 2 tọa độ (locSource và driverLocation)
        double lat1 = locSource.getLat();
        double lon1 = locSource.getLon();
        double lat2 = driverLocation.getLat();
        double lon2 = driverLocation.getLon();

        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance /2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 -a));
        return R * c; // Khoảng cách tính bằng Km.
    }


    // Tổng thể PostMapping("/request-driver") <-> @MessageMapping("/driver-response)
    // ===> PostMapping("/request-driver") <->    (đầu cắm 1) driverService (đầu cắm 2)   <---->  @MessageMapping("/driver-response)
    private final Map<Long, CompletableFuture<String>> driverResponseHandlers = new ConcurrentHashMap<>(); // dây nối 2 đầu cắm.

    public void registerDriverResponseHandler(Long driverId, CompletableFuture<String> futureResponse) { // đầu cắm (1) driverService <-> @PostMapping("/request-driver")
        driverResponseHandlers.put(driverId, futureResponse);
    }

    public void handleDriverResponse(Long driverId, String responseStatus) {// đầu cắm (2) driverService <-> @MessageMapping("/driver-response")
        CompletableFuture<String> futureResponse = driverResponseHandlers.get(driverId);
        if (futureResponse != null) {
            futureResponse.complete(responseStatus);
            // Xóa handler sau khi nhận được phản hồi
            driverResponseHandlers.remove(driverId);
        }
    }

    public Map<String, Object> getDrivers(int offset, int limit) {
        // câu truy vấn sql liên qan đến bảng drivers và map trong postgreSQL
        String sql = """
            SELECT d.driver_id, d.first_name || ' ' || d.last_name AS driver_name, d.email, d.phone, 
                   CASE 
                       WHEN m.driver_id IS NOT NULL THEN 'online'
                       ELSE 'offline'
                   END AS online,
                   d.status, 
                   d.created_at
            FROM drivers d
            LEFT JOIN map m ON d.driver_id = m.driver_id
            ORDER BY 
                CASE WHEN m.driver_id IS NOT NULL THEN 0 ELSE 1 END,
                CASE WHEN d.status = 'Approved' THEN 0 ELSE 1 END, 
                d.created_at DESC
            LIMIT ? OFFSET ?;
        """;//offset : startRecord

        List<DriverToAdmin> drivers = jdbcTemplate.query(sql, new Object[]{limit, offset}, (rs, rowNum) -> new DriverToAdmin(
                rs.getLong("driver_id"),
                rs.getString("driver_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("online"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("drivers", drivers);
        response.put("total", driverRepository.count());
        return response;
    }

    public void editDriver(Long driverId, SignupRequest formData) {
        Optional<Driver> optionalDriver = driverRepository.findById(driverId);
        if (optionalDriver.isPresent()) {
            Driver updateDriver = optionalDriver.get();
            updateDriver.setEmail(formData.getEmail());
            updateDriver.setPhone(formData.getPhone());
            updateDriver.setFirstName(formData.getFirstName());
            updateDriver.setLastName(formData.getLastName());
            updateDriver.setPassword(formData.getPassword());

            driverRepository.save(updateDriver);
        }
    }

    public void updateStatusDriver(Long driverId, String newStatus) {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Not found driver to update"));
        driver.setStatus(newStatus);
        driverRepository.save(driver);
    }
}
