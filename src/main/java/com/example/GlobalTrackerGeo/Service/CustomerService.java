package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.Customer;
import com.example.GlobalTrackerGeo.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Map<String, Object> getRidersForAdmin(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("status"), Sort.Order.desc("createdAt")));

        // Láy danh sách riders với điều kiện phân trang
        List<Customer> riders = customerRepository.findRidersWithPagination(pageRequest);
        long total = customerRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("riders", riders);
        response.put("total", total);

        return response;
    }

    public void updateRiderStatus(long riderId, String newStatus) {
        Customer rider = customerRepository.findById(riderId).orElseThrow(() -> new RuntimeException("Rider not found"));
        rider.setStatus(newStatus);
        customerRepository.save(rider);
    }
}
