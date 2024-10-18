package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TripService {

    private TripRepository tripRepository;
    private ObjectMapper objectMapper;
    private DriverRepository driverRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TripService(TripRepository tripRepository, ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.objectMapper = objectMapper;
    }

    // ------------------- CUSTOMER -----------------------
    // Customer
    @Transactional
    public Trip saveNewTrip(DriverRequest driverRequest, String button) {
        if (button.equals("search")) {
            Trip newTrip = new Trip();
            String tripId = UUID.randomUUID().toString();
            newTrip.setTripId(tripId);
            newTrip.setDriverId(driverRequest.getDriverId());
            newTrip.setStatus("2"); // đã có tài xế ấn accept
            newTrip.setCustomerId(driverRequest.getCustomerId());
            newTrip.setSource(driverRequest.getLoc_source().toString());
            newTrip.setDestination(driverRequest.getLoc_destination().toString());
            newTrip.setDistance(driverRequest.getDistance());
            newTrip.setRoute(""); // Thêm các cặp (lat, lon) mỗi 5s kể từ khi tài xế bắt đầu ấn accept.

            tripRepository.save(newTrip); // Lưu trip

            // Lưu luôn thông tin thanh toán mà người dùng chọn khi đặt trip
            paymentService.savePayment(driverRequest.getPaymentRequest(), tripId);

            return newTrip;
        }

        // Nút "Create"
        Trip newTrip1 = new Trip();
        String tripId1 = UUID.randomUUID().toString();
        newTrip1.setTripId(tripId1);
        newTrip1.setDriverId(null);
        newTrip1.setStatus("1"); // đã có tài xế ấn accept
        newTrip1.setCustomerId(driverRequest.getCustomerId());
        newTrip1.setSource(driverRequest.getLoc_source().toString());
        newTrip1.setDestination(driverRequest.getLoc_destination().toString());
        newTrip1.setDistance(driverRequest.getDistance());
        newTrip1.setRoute(""); // Thêm các cặp (lat, lon) mỗi 5s kể từ khi tài xế bắt đầu ấn accept.

        tripRepository.save(newTrip1); // Lưu trip

        // Lưu luôn thông tin thanh toán mà người dùng chọn khi đặt trip
        paymentService.savePayment(driverRequest.getPaymentRequest(), tripId1);

        return newTrip1;
    }

    // Driver Update driver khi tài xế ấn accept (nút create)
    public void updateDriver(Trip newTrip, Long driverId) {
        newTrip.setDriverId(driverId);
        newTrip.setStatus("2");
        tripRepository.save(newTrip);
    }

    // Thêm địa chỉ mới (lat, lon) vào route của bảng trip
    public void updateTripRoute(String tripId, double lat, double lon) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // Thêm vị trí mới vào route
            trip.addLocationToRoute(lat, lon);

            //Cập nhật lại trip trong database
            tripRepository.save(trip);
        } else {
            throw new RuntimeException("Trip not found withd ID: " + tripId);
        }
    }

    // SeeDetail
    public Trip getDetailTrip(String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);

        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();


        }
        return null;
    }

    // Customer Cancel Trip
    public void cancelTrip(String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // Kiểm tra lại status trip (đề phòng trên giao diện và PostgreSQL ko khớp)
            if (trip.getStatus().equals("1") || trip.getStatus().equals("2")) {
                // Cập nhật trạng thái đã hủy status "5"
                trip.setStatus("5");
//                trip.setDriverId(null);
                tripRepository.save(trip);

                // Gửi thông báo đến cho Driver Web đơn tripId bị hủy qua WebSocket (có thể dể bên backend)
                messagingTemplate.convertAndSend("/topic/cancel/" + trip.getDriverId(), tripId);
            } else {
                throw new IllegalStateException("Trip cannot be canceled at this stage.");
            }
        } else {
            throw  new RuntimeException("Trip not found with ID: " + tripId);
        }
    }

    // Hàm lấy ra danh sách tất cả các vị trí lưu trong route -> customer web, admin web
    public List<LocationNoName> getTripRoute(String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);

        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();
            String routeJson = trip.getRoute(); // Lấy chuỗi JSON từ trường route

            List<LocationNoName> locations = new ArrayList<>();

            try {
                // Chuyển chuỗi JSON thành ArrayNode
                ArrayNode routeArray = (ArrayNode) objectMapper.readTree(routeJson);

                // Lặp qua mỗi phần tử trong JSONArray và thêm vào danh sách locations
                for (JsonNode node : routeArray) {
                    double lat = node.get("lat").asDouble();
                    double lon = node.get("lon").asDouble();
                    LocationNoName location = new LocationNoName(lat, lon);
                    locations.add(location);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return locations;
        } else {
            throw  new RuntimeException("Trip not found with ID: " + tripId);
        }
    }

    // Hàm lấy tất cả chuến đi trong bảng trips của customer cụ thể
    public List<Trip> getTripsByCustomerId(Long customerId) {
        return tripRepository.findByCustomerIdOrderByStatusAscCreatedAtAsc(customerId);
    }

    // Test
    public void getLocationFromJsonDb(String tripId) throws IOException {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // Láy ra trường source trong chuyển sang đối tượng Location
            String jsonSource = trip.getSource();
            System.out.println("jsonSource: " + jsonSource);
            // Chuyển source vừa lấy -> Location
            Location location = Location.convertJsonToLocation(jsonSource);
            System.out.println("After convert JSON to Location, location:" + location.getLat() + ", " +  location.getLon());// in thử sau khi convert
        } else {
            throw new IOException("Error convertJsonToLocation");
        }
    }

    // ------------------- DRIVER ---------------------
    // Driver click Get It, Cancel, Received, Completed
    @Transactional  // đảm bảo các thay đổi được thực hiện trong một giao dịch (transactional)
    public void updateStatus(String tripId, Long driverId, String status) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();
            if ("1".equals(status)) {
                trip.setStatus(status); // update status
                trip.setDriverId(null); // driverId null để tài xế khác có thể nhận
                tripRepository.save(trip); // lưu lại
            } else if ("2".equals(status)){
                trip.setStatus(status);
                trip.setDriverId(driverId); // set driver vừa nhận
                tripRepository.save(trip);
            } else if ("4".equals(status)) {
                trip.setStatus(status);
                paymentService.setStatus(tripId); // set đã thanh toán
            }
            else {
                trip.setStatus(status);
                tripRepository.save(trip);
            }

        }
    }

    // ---------------------- ADMIN -----------------------
    // Ride Statistic
    public List<Trip> getTripsByStatusAndDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return tripRepository.findTripsByStatusAndDateRange(startDate, endDate);
    }

    // Recent Ride
//    public List<Trip> getRecentRides() {
//        return tripRepository.findTop10ByOrderByCreatedAtDesc();
//    }

    public Map<String, Object> getRecentRides(int offset, int limit) {
        // Câu truy vấn có thêm điều kiện OFFSET và LIMIT
        String sql = "SELECT t.trip_id, t.customer_id, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                "t.driver_id, CONCAT(d.first_name, ' ', d.last_name) AS driver_name, t.status, t.source, " +
                "t.destination, t.distance, t.route, t.created_at " +
                "FROM trips t " +
                "LEFT JOIN customers c ON t.customer_id = c.customer_id " +
                "LEFT JOIN drivers d ON t.driver_id = d.driver_id " +
                "WHERE t.status != '1' " +
                "ORDER BY t.created_at DESC " +
                "LIMIT ? OFFSET ?"; // offset này là startFromRecord = ? ko phải là pageNumber như mọi khi dùng PageRequest

        // Sử dụng jdbcTemplate để truyền tham số offset và limit
        List<TripToAdmin> trips = jdbcTemplate.query(sql, new Object[]{limit, offset}, (rs, rowNum) -> new TripToAdmin(
                rs.getString("trip_id"),
                rs.getLong("customer_id"),
                rs.getString("customer_name"),
                rs.getLong("driver_id"),
                rs.getString("driver_name"),
                rs.getString("status"),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getDouble("distance"),
                rs.getString("route"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("rides", trips);
        response.put("total", tripRepository.countTripsNotStatus1());

        return response;
    }

    public Map<String, Object> getResultReports(int offset, int limit) {
        String sql1 = """
                SELECT t.trip_id, CONCAT(d.first_name, ' ', d.last_name) AS driver_name, CONCAT(c.first_name, ' ', c.last_name) AS customer_name, t.created_at, p.total AS total_revenue,
                    p.total * 0.05 AS commission,
                    p.total * (1 -0.05) AS driver_payment_amount,
                    p.total * 0.05 AS admin_earning_amount,
                    'cash' AS payment_method
                FROM trips t 
                    LEFT JOIN drivers d ON t.driver_id = d.driver_id
                    LEFT JOIN customers c ON t.customer_id = c.customer_id
                    LEFT JOIN payments p ON t.trip_id = p.trip_id
                WHERE p.payment_status = 'Paid'
                ORDER BY t.created_at DESC
                LIMIT ? OFFSET ?
                """;

        List<ResultReport> resultReports = jdbcTemplate.query(sql1, new Object[]{limit, offset}, (rs, rowNum) -> new ResultReport(
                rs.getString("trip_id"),
                rs.getString("driver_name"),
                rs.getString("customer_name"),// đảm bảo đúng thứ tự của class ResultReport
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getDouble("total_revenue"),
                rs.getDouble("commission"),
                rs.getDouble("driver_payment_amount"),
                rs.getDouble("admin_earning_amount"),
                rs.getString("payment_method")
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("resultReports", resultReports);
        response.put("total", tripRepository.countCompletedTrips()); // đếm rides completed vì completed -> driver earn $

        return response;
    }

    public Map<String, Object> getDriverPaymentReport(int offset, int limit) {
        String sql = """
            SELECT d.driver_id, CONCAT(d.first_name, ' ', d.last_name) AS driver_name, b.account_no AS driver_account_no, b.name AS driver_bank_name, 
                COALESCE(SUM(p.total), 0) AS total_revenue, 
                COALESCE(SUM(p.total), 0) * 0.05 AS commission, 
                COALESCE(SUM(p.total), 0) * 0.95 AS driver_payment_amount, 
                'cash' AS payment_method
            FROM drivers d
            LEFT JOIN banks b ON d.driver_id = b.driver_id
            LEFT JOIN trips t ON d.driver_id = t.driver_id
            LEFT JOIN payments p ON t.trip_id = p.trip_id AND p.payment_status = 'Paid' AND t.status = '4'
            GROUP BY d.driver_id, CONCAT(d.first_name, ' ', d.last_name), b.account_no, b.name
            ORDER BY total_revenue DESC, d.created_at DESC
            LIMIT ? OFFSET ?;
            """; // chỉ dùng alias sau khi nó đã được tinh toán (khác cấp) vd ORDER BY total_revenue

        List<DriverPaymentReport> driverPaymentReports = jdbcTemplate.query(sql, new Object[]{limit, offset}, (rs, rowNum) -> new DriverPaymentReport(
                rs.getLong("driver_id"),
                rs.getString("driver_name"),
                rs.getString("driver_account_no"),
                rs.getString("driver_bank_name"),
                rs.getDouble("total_revenue"),
                rs.getDouble("commission"),
                rs.getDouble("driver_payment_amount"),
                rs.getString("payment_method")
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("resultReports", driverPaymentReports);
        response.put("total", 21);

        return response;
    }
}

