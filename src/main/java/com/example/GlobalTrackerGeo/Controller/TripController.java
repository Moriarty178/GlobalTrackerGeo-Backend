package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Customer;
import com.example.GlobalTrackerGeo.Entity.Payment;
import com.example.GlobalTrackerGeo.Entity.Rating;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.CustomerRepository;
import com.example.GlobalTrackerGeo.Repository.PaymentRepository;
import com.example.GlobalTrackerGeo.Repository.RatingRepository;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import com.example.GlobalTrackerGeo.Service.CustomerService;
import com.example.GlobalTrackerGeo.Service.TripService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // -------------- CUSTOMER WEB -----------------
    // Customer bảng My Trip
    @PostMapping("/my-trips") // customer web
    public ResponseEntity<List<Trip>> getMyTrips(@RequestBody TripRequest tripRequest) {
        Long customerId = tripRequest.getId();
        int offset = tripRequest.getOffset(); // số trang (pageNumber)

        // Tạo một PageRequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, 6, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

        // Lấy danh sách chuyến đi với điều kiện sắp xếp và phân trang (trip có customerId) - customer đặt
        Page<Trip> trips = tripRepository.findByCustomerId(customerId, pageRequest);

        return ResponseEntity.ok(trips.getContent());
    }

    // Customer cancel
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelTrip(@RequestBody Map<String, String> request) {
        try {
            tripService.cancelTrip(request.get("tripId"));
            // Thông báo cho tài xế qua websocket, dùng luôn "/topic/alert + driverId" thêm phần type = "cancelTrip"

            return ResponseEntity.ok("Trip canceled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error canceling trip!");
        }
    }

    // Customer rating
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found!");
        }
    }

    // Customer Web: nhận thông tin khi click 'View Driver'
    @MessageMapping("/driver-location-with-trip")
    public void sendDriverLocationToCustomerWeb(@RequestBody DriverLocationDTO driverLocationDTO) {
        // Gửi thông tin vị trí tài xế đến Customer Web
        messagingTemplate.convertAndSend("/topic/location-send-to-customer-web/" + driverLocationDTO.getDriverId(), driverLocationDTO);
    }
    // ==================================================

    // --------------- DRIVER WEB ----------------
    // Driver Web bảng Trips Received
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

    // Driver Web: Danh sách trips nhận thủ công
    @PostMapping("/trip-list")
    public ResponseEntity<List<Trip>> getTripListNone (@RequestBody Map<String, Integer> request) {
        // Tạo mọt PageRequest với phân trang và sắp xếp
        int offset = request.get("offsetList");
        PageRequest pageRequest = PageRequest.of(offset, 10, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("distance")));

        // Láy danh sách chuyến đi với điều kiện phân trang
        Page<Trip> trips = tripRepository.findByStatus("1", pageRequest);

        return ResponseEntity.ok(trips.getContent());
    }

    // Driver Web: click getIt, cancel, received, completed
    @PostMapping("/update-status-trip")
    public ResponseEntity<?> updateStatusTrip(@RequestBody UpdateStatusRequest updateStatusRequest) {
        try {
            tripService.updateStatus(updateStatusRequest.getTripId(), updateStatusRequest.getDriverId(), updateStatusRequest.getStatus());

            return ResponseEntity.ok("Trip updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.ok("Error updating trip!");
        }
    }

    // Driver Web, kểm tra status để tắt send_location_to_customer
    @GetMapping("/{driverId}/trips-status")
    public ResponseEntity<List<String>> getTripStatuses(@PathVariable Long driverId) {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();

        // Lấy thời điểm đầu ngày và cuối ngày
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<String> statuses = tripRepository.findStatusesByDriverId(driverId, startOfDay, endOfDay);
        if (statuses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Trả về 404
        }
        return ResponseEntity.ok(statuses);
    }

    // Láy thông tin chi tiết của một trip cụ thể
    @GetMapping("/{tripId}/route")
    public List<LocationNoName> getTripRoute(@PathVariable String tripId) {
        return tripService.getTripRoute(tripId);
    }
    // ==================================================

    // ----------------- ADMIN WEB ---------------------
    // Ride Status Chart
    @GetMapping("/api/ride-status-data")
    public Map<String, Object> getRideStatusData(@RequestParam int start, @RequestParam int limit) {
        // Tính toán khoảng thời gian dựa trên start và limit
        LocalDateTime endDate = LocalDateTime.now().minusMonths(start); // Thời gian hiện tại trừ đi số tháng start
        LocalDateTime startDate = endDate.minusMonths(limit);           // endDate trừ đi số tháng limit

        // Danh sách status cần lấy dữ liệu
        List<String> statuses = Arrays.asList("2", "3", "4", "5"); // Running, Completed, Canceled

        // Lấy danh sách các chuyến đi từ service
        List<Trip> trips = tripService.getTripsByStatusAndDateRange(startDate, endDate);

        // Nhóm các chuyến đi theo tháng và theo status
        Map<String, Map<String, Long>> ridesGroupedByMonthAndStatus = trips.stream()
                .collect(Collectors.groupingBy(trip -> trip.getCreatedAt().getYear() + "-" + trip.getCreatedAt().getMonthValue(),
                        Collectors.groupingBy(Trip::getStatus, Collectors.counting())));

        // Chuẩn bị dữ liệu để gửi cho biểu đồ
        List<String> labels = ridesGroupedByMonthAndStatus.keySet().stream().sorted().collect(Collectors.toList());

        // Chuẩn bị datasets cho từng loại status
        Map<String, List<Long>> statusDataMap = new HashMap<>();
        for (String status : statuses) {
            statusDataMap.put(status, labels.stream()
                    .map(label -> ridesGroupedByMonthAndStatus.getOrDefault(label, new HashMap<>()).getOrDefault(status, 0L))
                    .collect(Collectors.toList()));
        }

        // Kết hợp Running Rides (status 2 và 3)
        List<Long> runningRidesData = labels.stream()
                .map(label -> statusDataMap.getOrDefault("2", new ArrayList<>()).get(labels.indexOf(label))
                        + statusDataMap.getOrDefault("3", new ArrayList<>()).get(labels.indexOf(label)))
                .toList();

        // Tạo cấu trúc dữ liệu trả về frontend
        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("datasets", Arrays.asList(
                Collections.singletonMap("data", runningRidesData), // Running Rides
                Collections.singletonMap("data", statusDataMap.get("5")), // Canceled Rides
                Collections.singletonMap("data", statusDataMap.get("4"))  // Completed Rides
        ));

//        System.out.println("Total Running 2, 3:" + runningRidesData);
//        System.out.println("Status 2: " + ((List<Map<String, Object>>) response.get("datasets")).get(0));
        return response;
    }

    // Recent Ride + Load more, back
    @GetMapping("/recent-rides")
    public ResponseEntity<Map<String, Object>> getRecentRides(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
        ){
//        List<TripToAdmin> trips = tripService.getRecentRides(offset, limit);
//        Long total = tripRepository.countTripsNotStatus1();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("rides", trips);
//        response.put("total", total);

        return ResponseEntity.ok(tripService.getRecentRides(offset, limit));
    }

    // Customer controller
    @GetMapping("/riders")
    public ResponseEntity<Map<String, Object>> getRiders(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
        ) {

        return ResponseEntity.ok(customerService.getRidersForAdmin(offset, limit));
    }

    // RideHistory
    @GetMapping("/{riderId}/history")
    public ResponseEntity<Map<String, Object>> getTripHistory(@PathVariable long riderId, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        // Tạo một PageRequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("status"), Sort.Order.desc("createdAt")));

        Page<Trip> tripHistory = tripRepository.findByCustomerId(riderId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("total", tripRepository.countTripsByCustomerId(riderId));
        response.put("rides", tripHistory.getContent());

//        System.out.println("offset : limit <==>" + offset + " : " + limit);
//        System.out.println("Size ====" + tripHistory.getContent().size());

        return ResponseEntity.ok(response);
    }

    // RiderStatus
    @PostMapping("/riders/{riderId}/status")
    public ResponseEntity<?> updateRiderStatus(@PathVariable long riderId, @RequestBody Map<String, String> statusMap) {
        try {
            String newStatus = statusMap.get("status");
            customerService.updateRiderStatus(riderId, newStatus);
            return ResponseEntity.ok("Status updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updated rider status!");
        }

    }

    // Add Rider
    @PostMapping("/riders/add")
    public ResponseEntity<String> addRider(@RequestBody SignupRequest addRequest) {
        try {
            customerService.addRider(addRequest);
            return ResponseEntity.ok("Rider added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding rider!");
        }
//        Customer newRider = new Customer();
//        newRider.setEmail(addRequest.getEmail());
//        newRider.setPhone(addRequest.getPhone());
//        newRider.setFirstName(addRequest.getFirstName());
//        newRider.setLastName(addRequest.getLastName());
//        newRider.setPassword(addRequest.getPassword());
//
//        customerRepository.save(newRider);
//
//        return ResponseEntity.ok("Rider added successfully.");
    }

//    @PostMapping("/riders/edit")// thêm {riderId}
//    public ResponseEntity<?> editRider(@RequestBody SignupRequest editRequest) {
//        try {
//            customerService.editRider(editRequest);
//            return ResponseEntity.ok("Rider edited successfully.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error editing rider!");
//        }
//    }

    // Lấy formData của riderId lên cho edit rider
    @GetMapping("/riders/{riderId}")
    public ResponseEntity<?> getRiderDetails (@PathVariable long riderId) {
        return ResponseEntity.ok(customerRepository.findById(riderId));
    }

    // Edit rider
    @PutMapping("/riders/{riderId}")
    public ResponseEntity<?> editRider (@PathVariable long riderId, @RequestBody SignupRequest formData) {
        try {
            customerService.editRider(riderId, formData);
            return ResponseEntity.ok("Edited rider successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error editing rider!");
        }

//        Optional<Customer> optionalRider = customerRepository.findById(riderId);
//        if (optionalRider.isPresent()) {
//            Customer updateRider = optionalRider.get();
//
//            updateRider.setEmail(formData.getEmail());
//            updateRider.setPhone(formData.getPhone());
//            updateRider.setFirstName(formData.getFirstName());
//            updateRider.setLastName(formData.getLastName());
//            updateRider.setPassword(formData.getPassword());
//
//            customerRepository.save(updateRider);
//            return ResponseEntity.ok("Updated rider successfully.");
//        }
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found rider wid riderID:" + riderId);
    }

    // Delete rider
    @DeleteMapping("/riders/{riderId}")
    public ResponseEntity<?> deleteRider(@PathVariable long riderId) {
        try {
            customerRepository.deleteById(riderId);
            return ResponseEntity.ok("Deleted rider successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting rider with riderId: " + riderId);
        }
    }
}
