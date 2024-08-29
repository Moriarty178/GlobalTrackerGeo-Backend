package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.DriverLocation;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AdminWebService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String adminWebUrl = "http://admin-web-url/aip/admin/driver-location";

    public void sendToAdminWeb(DriverLocation driverLocation) {
        restTemplate.postForObject(adminWebUrl, driverLocation, Void.class);
    }
}
