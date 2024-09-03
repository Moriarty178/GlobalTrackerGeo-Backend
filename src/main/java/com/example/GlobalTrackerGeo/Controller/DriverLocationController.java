package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.DriverLocationDTO;
import com.example.GlobalTrackerGeo.Entity.Alert;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Repository.AlertRepository;
import com.example.GlobalTrackerGeo.Repository.DriverLocationRepository;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import com.example.GlobalTrackerGeo.Service.DriverLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class DriverLocationController {

    private final static double SPEED_LIMIT = 50.0;

    @Autowired
    private DriverLocationService driverLocationService;

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private DriverLocationRepository driverLocationRepository;
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver-location")//tự động map tâất cả tin nhắn gửi từ Driver Web đến endpoint "/driver-location" để nó xử lý.
    public void handleDriverLocation(@RequestBody DriverLocationDTO location) {
        //Lưu thông tin vị trí vào PostgreSQL và Redis Stream
        driverLocationService.saveLocation(location);

        //Gửi thông tin vị trí đến Admin Web qua WebSocket
        messagingTemplate.convertAndSend("/topic/driver-location", location);

        //Phân tích và cảnh bảo ex: quá tốc độ, vào vùng cấm (GeoZone), vv...
        if (location.getSpeed() > SPEED_LIMIT) {// sau này dùng 1 hàm riêng phân tích điều kiện gửi alert, ko chỉ quá tốc độ mà còn dựa trên khu vực địa lý vv...
            //gửi cảnh báo đến topic của tài xế cụ thể
            messagingTemplate.convertAndSend("/topic/alert/" + location.getDriverId(), createdAndSaveAlert(location));
        }

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
