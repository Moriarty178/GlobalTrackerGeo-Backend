package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.*;
import com.example.GlobalTrackerGeo.Repository.*;
import com.example.GlobalTrackerGeo.Service.DriverLocationService;
import com.example.GlobalTrackerGeo.Service.DriverService;
import com.example.GlobalTrackerGeo.Service.MapService;
import com.example.GlobalTrackerGeo.Service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private TripService tripService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private TripRepository tripRepository;

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
        //System.out.println("loc_source latitude:" + driverRequest.getLoc_source().getLat());
        //System.out.println("loc_source longitude:" + driverRequest.getLoc_source().getLon());
        //System.out.println("loc_source displayname:" + driverRequest.getLoc_source().getDisplayName());
        // Gửi thông tin đơn hàng (driverRequest) đến tái xế qua websocket
        messagingTemplate.convertAndSend("/topic/alert/" + driverRequest.getDriverId(), driverRequest);

        // Thêm phần thời gian chờ = 10s. Sau 10s tài xế ko ấn thì phản hồi lại cho customer để chọn tài xế khác ??????????

        // Chờ phản hồi từ Driver Web qua websocket "/app/driver-response"
        CompletableFuture<String> futureResponse = new CompletableFuture<>();
        driverService.registerDriverResponseHandler(driverRequest.getDriverId(), futureResponse);// lắng nghe

        try {
            String driverResponse = futureResponse.get(15, TimeUnit.SECONDS);//đợi tối đa 15s cho tài xế phản hồi
            // Trả thông báo khi tái xế không có phản hồi accept hay deny


            if ("accepted".equals(driverResponse)) {
                double estimatedTime = 15.0;//calculateEstimatedTime(driverRequest.getDistance());
                // Lưu trip và payment tương ứng vào database
                tripService.saveNewTrip(driverRequest, "search"); // status trip "2"

                // Test chuyển đổi chuỗi source, destiantion trong PostgreSQL -> Location
                tripService.getLocationFromJsonDb("1e065510-29dc-47a1-8075-abb3544b5e06");

                // Test add route
                tripService.updateTripRoute("1e065510-29dc-47a1-8075-abb3544b5e06", 25.0285, 132.6789);

                return ResponseEntity.ok(java.util.Map.of(
                        "status", "accepted",
                        "distance", driverRequest.getDistance(),
                        "estimatedTime", estimatedTime
//                        "lat", "latitude",
//                        "lon", "longitude"
                ));
            } else {
                return ResponseEntity.ok(java.util.Map.of("status", "declined"));
            }
        } catch (TimeoutException e) { // Trường hợp tài xế không phản hồi trong 15 giây
            return ResponseEntity.ok(java.util.Map.of("status", "timeout", "message", "Driver did not respond in time."));
//            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)// gửi mã code 408 -> fe nhận là lỗi -> catch
//                    .body(java.util.Map.of("status", "timeout", "message", "Driver did not respond in time."));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Driver response timed out.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @MessageMapping("/driver-response")
    public void handlerDriverResponse(@RequestBody DriverResponse driverResponse) {
        // Phản hồi về Backend thông qua CompletableFuture
        driverService.handleDriverResponse(driverResponse.getDriverId(), driverResponse.getStatus());
    }

    @PostMapping("/request-driver-v2") // Khi customer ấn "create" -> payment form -> backend
    public ResponseEntity<?> requestDriverV2(@RequestBody DriverRequest driverRequest) {
        // Tạo trip và payment lưu vào database và láy trip để khi tài xế ấn "accepted" => update
        Trip newTrip = tripService.saveNewTrip(driverRequest, "create"); // status trip "1" & driverId "null"

        // Tìm kiếm tài xế gần loc_source <=> search-drivers
        List<DriverDTO> driverList = driverService.findNearestDrivers(driverRequest.getLoc_source());
        System.out.println("Driver List size:" + driverList.size());

        // Lặp qua danh sách tài xế đề xuất -> gửi yêu cầu cho tài xế -> return khi response = "accepted"
        for (DriverDTO driver : driverList) {
            // Gửi thông tin và chờ phản hồi
            messagingTemplate.convertAndSend("/topic/alert/" + driver.getDriverId(), driverRequest);

            // Chờ phản hồi của tái xế
            CompletableFuture<String> futureResponse = new CompletableFuture<>();
            driverService.registerDriverResponseHandler(driver.getDriverId(), futureResponse);

            try {
                String driverResponse = futureResponse.get(10, TimeUnit.SECONDS); // đợi phản hồi tối đa 10s
                double estimatedTime = 15.0;
                // Nếu tài xế nào đồng ý thì gửi thông tin lại customer web;
                if ("accepted".equals(driverResponse)) {
                    // set driver
                    tripService.updateDriver(newTrip, driver.getDriverId());
                    return ResponseEntity.ok(java.util.Map.of(
                            "status", "accepted",
                            "distance", driver.getDistance(),// khoảng cách driver với loc_source
                            "estimatedTime", estimatedTime));
                }
            } catch (InterruptedException | ExecutionException e) {
                return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Driver response timed out.");
            } catch (TimeoutException e) {
                continue; // nếu tài xế hiện tại ko phản hồi -> TimeoutException -> continue để duyệt tiếp tài xế khác
            }
        }

        // Nếu không tài xế nào accept hoặc không tìm được tài xế nào <=> Trip không được nhận
        return ResponseEntity.ok(java.util.Map.of("status", "The trip has not been accepted by any driver."));
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
