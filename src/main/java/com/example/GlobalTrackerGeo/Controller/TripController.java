package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Payment;
import com.example.GlobalTrackerGeo.Entity.Rating;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.PaymentRepository;
import com.example.GlobalTrackerGeo.Repository.RatingRepository;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import com.example.GlobalTrackerGeo.Service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @PostMapping("/my-trips") // customer web
    public ResponseEntity<List<Trip>> getMyTrips(@RequestBody TripRequest tripRequest) {
        Long customerId = tripRequest.getId();
        int offset = tripRequest.getOffset(); // số trang (pageNumber)

        // Tạo một PageRequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, 10, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

        // Lấy danh sách chuyến đi với điều kiện sắp xếp và phân trang (trip có customerId) - customer đặt
        Page<Trip> trips = tripRepository.findByCustomerId(customerId, pageRequest);

        return ResponseEntity.ok(trips.getContent());
    }

    @PostMapping("/trips-received") // driver web
    public ResponseEntity<List<Trip>> getTripsReceived(@RequestBody TripRequest tripRequest) {
        Long driverId = tripRequest.getId();
        int offset = tripRequest.getOffset();

        // Tạo một Pagerequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, 10, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

        // Lấy danh sách chuyến đi với điều kiện phân trang (trip có driverId) - driver đã nận
        Page<Trip> trips = tripRepository.findByDriverId(driverId, pageRequest);

        return ResponseEntity.ok(trips.getContent());
    }

    @PostMapping("/update-status-trip")
    public ResponseEntity<?> updateStatusTrip(@RequestBody UpdateStatusRequest updateStatusRequest) {
        try {
            tripService.updateStatus(updateStatusRequest.getTripId(), updateStatusRequest.getDriverId(), updateStatusRequest.getStatus());

            return ResponseEntity.ok("Trip updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.ok("Error updating trip.");
        }
    }

    @PostMapping("/trip-list")
    public ResponseEntity<List<Trip>> getTripListNone (@RequestBody Map<String, Integer> request) {
        // Tạo mọt PageRequest với phân trang và sắp xếp
        int offset = request.get("offsetList");
        PageRequest pageRequest = PageRequest.of(offset, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("distance")));

        // Láy danh sách chuyến đi với điều kiện phân trang
        Page<Trip> trips = tripRepository.findByStatus("1", pageRequest);

        return ResponseEntity.ok(trips.getContent());
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelTrip(@RequestBody Map<String, String> request) {
        try {
            tripService.cancelTrip(request.get("tripId"));
            // Thông báo cho tài xế qua websocket, dùng luôn "/topic/alert + driverId" thêm phần type = "cancelTrip"

            return ResponseEntity.ok("Trip canceled successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error canceling trip");
        }
    }

    @PostMapping("/rate-trip")
    public ResponseEntity<String> rateTrip(@RequestBody RatingRequest request) {
        Optional<Trip> optionalTrip = tripRepository.findById(request.getTripId());

        if (optionalTrip.isPresent()) {
            // Kiểm tra xem đã có trip đã có rating chưa, chưa có -> add vào, có rồi -> phản hồi lại customer web
            if (ratingRepository.findByTripId(request.getTripId()).isEmpty()) {
                Rating newRating = new Rating();
                newRating.setRatingId(UUID.randomUUID().toString());
                newRating.setTripId(request.getTripId());
                newRating.setRating(request.getRating());
                newRating.setFeedback(request.getFeedback());

                ratingRepository.save(newRating);

                return ResponseEntity.ok("Rating submitted successfully.");
            } else {
                return ResponseEntity.ok("The trip has a rating!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found.");
        }
    }

    @MessageMapping("/driver-location-with-trip")
    public void sendDriverLocationToCustomerWeb(@RequestBody DriverLocationDTO driverLocationDTO) {
        // Gửi thông tin vị trí tài xế đến Customer Web
        messagingTemplate.convertAndSend("/topic/location-send-to-customer-web/" + driverLocationDTO.getDriverId(), driverLocationDTO);
    }

    // Láy thông tin chi tiết của một trip cụ thể
    @GetMapping("/{tripId}/route")
    public List<LocationNoName> getTripRoute(@PathVariable String tripId) {
        return tripService.getTripRoute(tripId);
    }
}
