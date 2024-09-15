package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.LocationNoName;
import com.example.GlobalTrackerGeo.Dto.TripRequest;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import com.example.GlobalTrackerGeo.Service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService tripService;
    @Autowired
    private TripRepository tripRepository;

//    @PostMapping("/my-trips")
//    public ResponseEntity<List<Trip>> getCustomerTrips(@RequestBody Map<String, Long> request) {
//        Long customerId = request.get("customerId");
//        List<Trip> trips = tripService.getTripsByCustomerId(customerId);
//        return ResponseEntity.ok(trips);
//    }

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

    @GetMapping("/{tripId}/route")
    public List<LocationNoName> getTripRoute(@PathVariable String tripId) {
        return tripService.getTripRoute(tripId);
    }
}
