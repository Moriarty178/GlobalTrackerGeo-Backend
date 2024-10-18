package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.*;
import com.example.GlobalTrackerGeo.Repository.*;
import com.example.GlobalTrackerGeo.Service.CustomerService;
import com.example.GlobalTrackerGeo.Service.DriverService;
import com.example.GlobalTrackerGeo.Service.TripService;
import com.example.GlobalTrackerGeo.Service.VehicleService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private DriverService driverService;
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // -------------- CUSTOMER WEB -----------------
    // Customer bảng My Trip
    @PostMapping("/my-trips") // customer web   REPLACE -> "{riderId}/history" *****************
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
    @PostMapping("/trips-received") // driver web  REPLACE -> "/drivers/{driverId}/history*************************
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
    //----------------- Dashboard ----------->
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

        // Nhóm các chuyến đi theo tháng và theo status// định dạng tháng yyyy-MM (%02d) đảm bảo tháng luôn có 2 chữ số
        Map<String, Map<String, Long>> ridesGroupedByMonthAndStatus = trips.stream()
                .collect(Collectors.groupingBy(trip -> trip.getCreatedAt().getYear() + "-" + String.format("%02d", trip.getCreatedAt().getMonthValue()),
                        Collectors.groupingBy(Trip::getStatus, Collectors.counting())));

        // Chuẩn bị dữ liệu để gửi cho biểu đồ
        List<String> labels = ridesGroupedByMonthAndStatus.keySet().stream()
                .sorted(Comparator.comparing(label -> LocalDate.parse(label + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .collect(Collectors.toList());

        //List<String> labels = ridesGroupedByMonthAndStatus.keySet().stream().sorted().collect(Collectors.toList());
        //sout -> Labels: [2011-10, 2011-12, '2012-10!!!', 2012-2, 2012-9] lỗi thứ tự -> sx theo từ diển==> muốn dùng phải chuyển sang yyyy-mm (2024-02)

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

        System.out.println("Labels (Sorted): " + labels);
        return response;
    }


    // Recent Ride + Load more, back
    @GetMapping("/recent-rides")
    public ResponseEntity<Map<String, Object>> getRecentRides(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit){

        return ResponseEntity.ok(tripService.getRecentRides(offset, limit));
    }

    //----------------- Customer tab ----------->
    // Customer controller
    @GetMapping("/riders")
    public ResponseEntity<Map<String, Object>> getRiders(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit){

        return ResponseEntity.ok(customerService.getRidersForAdmin(offset, limit));
    }

    // RideHistory
    @GetMapping("/riders/history/{riderId}")
    public ResponseEntity<Map<String, Object>> getTripHistory(@PathVariable long riderId, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        // Tạo một PageRequest với phân trang và sắp xếp
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("status"), Sort.Order.desc("createdAt")));

        Page<Trip> tripHistory = tripRepository.findByCustomerId(riderId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("total", tripRepository.countTripsByCustomerId(riderId));
        response.put("trips", tripHistory.getContent());

//        System.out.println("offset : limit <==>" + offset + " : " + limit);
//        System.out.println("Size ====" + tripHistory.getContent().size());

        return ResponseEntity.ok(response);
    }

    // RiderStatus
    @PutMapping("/riders/status/{riderId}")
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
    }

    // Lấy formData của riderId lên cho edit rider
    @GetMapping("/riders/{riderId}")
    public ResponseEntity<?> getRiderDetails (@PathVariable long riderId) {
        return ResponseEntity.ok(customerRepository.findById(riderId));
    }

    // Edit rider
    @PutMapping("/riders/edit/{riderId}")
    public ResponseEntity<?> editRider (@PathVariable long riderId, @RequestBody SignupRequest formData) {
        try {
            customerService.editRider(riderId, formData);
            return ResponseEntity.ok("Edited rider successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error editing rider!");
        }
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

    //----------------- Drivers tab ----------->
    // Drivers list
    @GetMapping("/drivers")
    public ResponseEntity<Map<String, Object>> getDrivers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
        ){

        return ResponseEntity.ok(driverService.getDrivers(offset, limit));
    }

    // Add Driver
    @PostMapping("/drivers/add")
    public ResponseEntity<?> addDriver(@RequestBody SignupRequest signupRequest) {
        try {
            if (driverRepository.findByEmail(signupRequest.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists, try another email!");
            }
            Driver newDriver = new Driver();
            newDriver.setEmail(signupRequest.getEmail());
            newDriver.setPassword(signupRequest.getPassword());
            newDriver.setPhone(signupRequest.getPhone());
            newDriver.setFirstName(signupRequest.getFirstName());
            newDriver.setLastName(signupRequest.getLastName());
            newDriver.setStatus("Approved");

            driverRepository.save(newDriver);
            return ResponseEntity.ok("Driver added successfully.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data provided!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding the driver!");
        }
    }

    // Status Driver
    @PutMapping("/drivers/status/{driverId}")
    public ResponseEntity<?> updateStatusDriver(@PathVariable Long driverId, @RequestBody Map<String, String> statusMap) {
        try {
            String newStatus = statusMap.get("status");
            driverService.updateStatusDriver(driverId, newStatus);
            return ResponseEntity.ok("Driver status updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating status driver");
        }
    }

    // History Driver
    @GetMapping("/drivers/history/{driverId}")
    public ResponseEntity<Map<String, Object>> getDriverHistory(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.asc("status"), Sort.Order.asc("createdAt")));
        Page<Trip> trips = tripRepository.findByDriverId(driverId, pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("trips", trips.getContent());
        response.put("total", tripRepository.countTripsByDriverId(driverId));
        return ResponseEntity.ok(response);
    }

    // Statement Driver, get data for stats card
    @GetMapping("/drivers/stats/{driverId}")
    public ResponseEntity<Map<String, Object>> getStatsDriver(@PathVariable Long driverId) {

        Map<String, Object> response = new HashMap<>();
        response.put("totalTrips", tripRepository.countTripsOfDriver(driverId));
        response.put("canceledTrips", tripRepository.countCanceledTripsOfDriver(driverId));
        response.put("completedTrips", tripRepository.countCompletedTripsOfDriver(driverId));
        response.put("revenue", paymentRepository.calculateRevenueOfDriver(driverId));
        return ResponseEntity.ok(response);
    }

    // get driver to Edit
    @GetMapping("/drivers/{driverId}")
    public ResponseEntity<?> getDriverDetail (@PathVariable Long driverId) {
        return ResponseEntity.ok(driverRepository.findById(driverId));
    }

    // Edit Driver
    @PutMapping("/drivers/edit/{driverId}")
    public ResponseEntity<?> editDriver(@PathVariable Long driverId, @RequestBody SignupRequest formData) {
        try {
            driverService.editDriver(driverId, formData);
            return ResponseEntity.ok("Driver updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not Found driver to update!");
        }
    }

    // ------------ Un-approved tab
    @GetMapping("/drivers/un-approved")
    public ResponseEntity<Map<String, Object>> getDriversUnapproved(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("status")));
        Page<Driver> drivers = driverRepository.findByStatus("Pending",pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("drivers", drivers.getContent());
        response.put("total", driverRepository.countDriversUnapproved());

        return ResponseEntity.ok(response);
    }

    // ------------ Vehicle Type
    @GetMapping("/vehicles")
    public ResponseEntity<Map<String, Object>> getVehicles(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("vehicles", vehicles.getContent());
        response.put("total", vehicleRepository.count());

        return ResponseEntity.ok(response);
    }

    // Vehicle Add
    @PostMapping("/vehicles")
    public ResponseEntity<String> addVehicleType( // bên fe gửi formData dạng Multipart/form-data -> spring có thể dùng RequestParam để ánh xạ các trường tron form (key-value) vào biến cụ thể.
            @RequestParam("name") String name,
            @RequestParam("cost") Double cost,
            @RequestParam("status") String status,
            @RequestParam("image") MultipartFile image) {

        // Xác định vị trí lưu ảnh
        String imagePath = "D:/GlobalTrackerGeo/images/" + image.getOriginalFilename();
        File imageFile = new File(imagePath); // tạo đối tượng File cho phép lưu nội dung -> vào path

        try {
            // Lưu tệp ảnh
            image.transferTo(imageFile);

            // save -> db
            vehicleService.saveVehicle(name, cost, status, image.getOriginalFilename()); // đổi tên file thành định danh duy nhất

            return ResponseEntity.ok("Thêm loại xe thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra khi tải ảnh lên.");
        }
    }

//    @GetMapping("/vehicles/{vehicleId}")
//    public ResponseEntity<?> getVehicleDetail(@PathVariable Long vehicleId) {
//        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
//        if (optionalVehicle.isPresent()) {
//            return ResponseEntity.ok(optionalVehicle.get());
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy vehicle với ID: " + vehicleId);
//    }
    // Get Vehicle Type to Edit
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<?> getVehicleDetail(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vehicle với ID:" + vehicleId)));// nếu xử lý ngoại lệ sai, spring trả về 403 để ngăn cản truy cập tài nguyên dù tài nguyên có tồn tại
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ResponseEntity<?> editVehicle(
            @PathVariable Long vehicleId,
            @RequestParam("name") String name,
            @RequestParam("cost") Double cost,
            @RequestParam("status") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);

        if (optionalVehicle.isPresent()) {
            Vehicle editVehicle = optionalVehicle.get();
            editVehicle.setName(name);
            editVehicle.setCost(cost);
            editVehicle.setStatus(status);

            // Xử lý cập nhật ảnh nếu người dùng upload ảnh mới
            if (image != null && !image.isEmpty()) {
                String imagePath = "D:/GlobalTrackerGeo/images/" + image.getOriginalFilename();
                File imageFile = new File(imagePath);

                try {
                    // Lưu ảnh mới
                    image.transferTo(imageFile);
                    // Cập nhật tên ảnh vào database
                    editVehicle.setImg(image.getOriginalFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi khi sửa lại ảnh");
                }
            }

            // Lưu thay đổi vào cơ sở dữ liệu
            vehicleRepository.save(editVehicle);

            return ResponseEntity.ok("Chỉnh sửa thành công vehicle type.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy vehicle với vehicleID: " + vehicleId);
    }

    // Vehicles Delete
    @DeleteMapping("/vehicles/delete/{vehicleId}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isPresent()) {
            Vehicle deleteVehicle = optionalVehicle.get();

            vehicleRepository.delete(deleteVehicle);

            return ResponseEntity.ok("Deleted vehicleID :" + vehicleId);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found vehicle wid ID: " + vehicleId);
    }


    // -------------- Earning Reports
    @GetMapping("/earning-admin-report")
    public ResponseEntity<Map<String, Object>> getResultReports(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tripService.getResultReports(offset, limit));
    }

    @GetMapping("/driver-payment-report")
    public ResponseEntity<Map<String, Object>> getDriverPaymentReport(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tripService.getDriverPaymentReport(offset, limit));
    }
}

















