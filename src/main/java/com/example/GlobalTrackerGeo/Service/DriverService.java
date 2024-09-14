package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.DriverDTO;
import com.example.GlobalTrackerGeo.Dto.DriverResponse;
import com.example.GlobalTrackerGeo.Dto.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DriverService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


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

            Location driverLocation = new Location(latitude, longitude);
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


    private final Map<Long, CompletableFuture<String>> driverResponseHandlers = new ConcurrentHashMap<>();

    public void registerDriverResponseHandler(Long driverId, CompletableFuture<String> futureResponse) {
        driverResponseHandlers.put(driverId, futureResponse);
    }

    public void handleDriverResponse(Long driverId, String responseStatus) {
        CompletableFuture<String> futureResponse = driverResponseHandlers.get(driverId);
        if (futureResponse != null) {
            futureResponse.complete(responseStatus);
            // Xóa handler sau khi nhận được phản hồi
            driverResponseHandlers.remove(driverId);
        }
    }
}
