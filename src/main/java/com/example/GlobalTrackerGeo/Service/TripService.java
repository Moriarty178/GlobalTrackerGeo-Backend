package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Dto.Location;
import com.example.GlobalTrackerGeo.Entity.Trip;
import com.example.GlobalTrackerGeo.Repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

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


    // Test
    public void getLocationFromJsonDb(String tripId) throws IOException {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            Trip trip = optionalTrip.get();

            // Láy ra trường source trong chuyển sang đối tượng Location
            String jsonSource = trip.getSource();
            System.out.println("jsonSource: " + jsonSource);
            // Chuyển source vừa lấy -> Location
            Location location = new Location();
            location = Location.convertJsonToLocation(jsonSource);
            System.out.println("lat, lon source -> location:" + location.getLat() + ", " +  location.getLon());// in thử sau khi convert
        } else {
            throw new IOException("Error convertJsonToLocation");
        }
    }

}
