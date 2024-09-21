package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.*;
import com.example.GlobalTrackerGeo.Entity.Payment;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripService {

    private TripRepository tripRepository;
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private PaymentService paymentService;

    public TripService(TripRepository tripRepository, ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.objectMapper = objectMapper;
    }

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

    // Update driver khi tài xế ấn accept (nút create)
    public void updateDriver(Trip newTrip, Long driverId) {
        newTrip.setDriverId(driverId);
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

    // Cancel Trip
    public void cancelTrip(String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // Kiểm tra lại status trip (đề phòng trên giao diện và PostgreSQL ko khớp)
            if (trip.getStatus().equals("1") || trip.getStatus().equals("2")) {
                // Cập nhật trạng thái đã hủy status "5"
                trip.setStatus("5");
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
            } else {
                trip.setStatus(status);
                tripRepository.save(trip);
            }

        }
    }
}

