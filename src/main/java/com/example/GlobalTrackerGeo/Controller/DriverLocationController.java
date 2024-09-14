package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Alert;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Entity.Map;
import com.example.GlobalTrackerGeo.Repository.AlertRepository;
import com.example.GlobalTrackerGeo.Repository.DriverLocationRepository;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import com.example.GlobalTrackerGeo.Repository.MapRepository;
import com.example.GlobalTrackerGeo.Service.DriverLocationService;
import com.example.GlobalTrackerGeo.Service.DriverService;
import com.example.GlobalTrackerGeo.Service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api")
public class DriverLocationController {

    private final static double SPEED_LIMIT = 50.0;

    @Autowired
    private DriverService driverService;
    @Autowired
    private DriverLocationService driverLocationService;
    @Autowired
    private MapService mapService;


    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver-location")//Lăng nghe thông tin Driver Web pub qua topic = "/app/driver-location" sau đó xử lý.
    public void handleDriverLocation(@RequestBody DriverLocationDTO location) {
        //Lưu thông tin vị trí vào PostgreSQL và Redis Stream
        driverLocationService.saveLocation(location);

        //Gửi thông tin vị trí đến Admin Web qua WebSocket
        messagingTemplate.convertAndSend("/topic/driver-location", location);

        // Thêm tất cả tài xế vào bảng "Map" để sau này lấy lại cập nhật map, điều hướng, đề xuất.
        mapService.saveOrUpdateDriverLocationToMap(location);

        //Phân tích và cảnh bảo ex: quá tốc độ, vào vùng cấm (GeoZone), vv...
        if (location.getSpeed() > SPEED_LIMIT) {// sau này dùng 1 hàm riêng phân tích điều kiện gửi alert, ko chỉ quá tốc độ mà còn dựa trên khu vực địa lý vv...
            //gửi cảnh báo đến topic của tài xế cụ thể
            messagingTemplate.convertAndSend("/topic/alert/" + location.getDriverId(), createdAndSaveAlert(location));
        }

    }

    @PostMapping("/search-drivers")
    public ResponseEntity<List<DriverDTO>> searchDriver(@RequestBody LocationRequest locationRequest) {
        List<DriverDTO> nearestDrivers = driverService.findNearestDrivers(locationRequest.getLocSource());
        return ResponseEntity.ok(nearestDrivers);
    }

    @PostMapping("/request-driver")
    public ResponseEntity<?> requestDriver(@RequestBody DriverRequest driverRequest) {
        // Gửi thông tin đơn hàng (driverRequest) đến tái xế qua websocket
        messagingTemplate.convertAndSend("/topic/alert/" + driverRequest.getDriverId(), driverRequest);

        // Chờ phản hồi từ Driver Web qua websocket "/app/driver-response"
        CompletableFuture<String> futureResponse = new CompletableFuture<>();
        driverService.registerDriverResponseHandler(driverRequest.getDriverId(), futureResponse);

        try {
            String driverResponse = futureResponse.get(30, TimeUnit.SECONDS);

            if ("accepted".equals(driverResponse)) {
                double estimatedTime = 15.0;//calculateEstimatedTime(driverRequest.getDistance());
                // Thêm phân tạo trip và lưu vào csdl

                return ResponseEntity.ok(java.util.Map.of(
                        "status", "accepted",
                        "distance", driverRequest.getDistance(),
                        "estimatedTime", estimatedTime
                ));
            } else {
                return ResponseEntity.ok(java.util.Map.of("status", "declined"));
            }
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Driver response timed out.");
        }
    }
    @MessageMapping("/driver-response")
    public void handlerDriverResponse(@RequestBody DriverResponse driverResponse) {
        // Phản hồi về Backend thông qua CompletableFuture
        driverService.handleDriverResponse(driverResponse.getDriverId(), driverResponse.getStatus());
    }

    // Lấy tất danh sách tất cả tài xế kèm vị trí trong GlobalTrackerGeo
    @GetMapping("/all-driver-location")
    public List<Map> getAllMap() {
        //return mapRepository.findAll();
        return mapService.getAllDriverActive();
    }

    // Xử lý khi tài xế đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<?> handleDriverLogout(@RequestBody LogoutRequest logoutRequest) {
        long driverId = logoutRequest.getDriverId();

        // Ví dụ: xóa vị trí của tài ế trong Redis, một danh sách lưu vị trí tài xế được cập nhật theo Admin Map. Có thể sử dụng danh sách, Redis này để sau này làm phần đề xuất, điều hướng.
        //redisTemplate.delete("driver:" + driverId);

        // Xóa record của tài xế khỏi bảng 'Map' (xóa khỏi danh sách tài xế trong mạng lưới GlobalTrackerGeo)
        mapService.removeDriverFromMap(driverId);

        //Gửi thống báo đến Admin Web để cập nhật bản đồ
        messagingTemplate.convertAndSend("/topic/remove-driver", driverId);

        return ResponseEntity.ok("Driver logged out successfully.");
    }

    //private boolean shouldSendAlert(DriverLocationDTO location) {...}

    private Alert createdAndSaveAlert(DriverLocationDTO location) {//tạo alert từ thông tin vị trị FE Driver Web (DriverLocationDTO) gửi sang
        Alert alert = new Alert();
        Driver driver = driverRepository.findById(location.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
        alert.setAlertType("Over Speed");
        alert.setDriver(driver);
        alert.setMessage("Yor are going over the speed limit.");

        //Lưu alert vừa tạo vào csdl
        alertRepository.save(alert);
        return alert;
    }
}
