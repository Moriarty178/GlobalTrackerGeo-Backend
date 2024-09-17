package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.LocationNoName;
import com.example.GlobalTrackerGeo.Dto.RatingRequest;
import com.example.GlobalTrackerGeo.Dto.RequestCancelTrip;
import com.example.GlobalTrackerGeo.Dto.TripRequest;
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

    @PostMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(@RequestBody TripRequest tripRequest) {
        Long customerId = tripRequest.getCustomerId();
        int offset = tripRequest.getOffset(); // số trang (pageNumber)

        // Tạo một PageRequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, 10, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

        // Lấy danh sách chuyến đi với điều kiện sắp xếp và phân trang
        Page<Trip> trips = tripRepository.findByCustomerId(customerId, pageRequest);

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

    // Láy thông tin chi tiết của một trip cụ thể
    @GetMapping("/{tripId}/route")
    public List<LocationNoName> getTripRoute(@PathVariable String tripId) {
        return tripService.getTripRoute(tripId);
    }
}
