package com.example.GlobalTrackerGeo.Service;

import com.example.GlobalTrackerGeo.Entity.Customer;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Repository.CustomerRepository;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
//Tạo bean (CustomUserDetailsService) triển khai interface UserDetailsService ĐỂ CÓ THỂ tạo bean UserDetailsService. Thì các class dependency injection (dj) nó mới tạo bean được, vd:JwtRequestFilter.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private CustomerRepository customerRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        //Tìm tài xế theo email trong cơ sở dữ liệu
        Driver driver = driverRepository.findByEmail(email);
        if(driver == null) {
            //Tìm tài xế theo email trong cơ sở dữ liệu
            Customer customer = customerRepository.findByEmail(email);
            if(customer == null) {
                throw new UsernameNotFoundException("Customer not found with email: " + email);
            }
            //Trả về đối tượng UserDetails với thông tin customer
            return new org.springframework.security.core.userdetails.User(customer.getEmail(), customer.getPassword(), new ArrayList<>());
        }

        //Trả về đối tượng UserDetails với thông tin driver
        return new org.springframework.security.core.userdetails.User(driver.getEmail(), driver.getPassword(), new ArrayList<>());
    }
}
