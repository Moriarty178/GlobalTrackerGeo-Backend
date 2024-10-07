package com.example.GlobalTrackerGeo.Repository;

import com.example.GlobalTrackerGeo.Entity.Customer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByEmail(String email);

    // Total customers
    long count();

    @Query("SELECT c FROM Customer c ORDER BY c.status DESC, c.createdAt DESC")
    List<Customer> findRidersWithPagination(PageRequest pageRequest);
}
