package com.example.GlobalTrackerGeo.Controller;

import com.example.GlobalTrackerGeo.Dto.AuthenticationRequest;
import com.example.GlobalTrackerGeo.Dto.LoginResponse;
import com.example.GlobalTrackerGeo.Dto.SignupRequest;
import com.example.GlobalTrackerGeo.Entity.Driver;
import com.example.GlobalTrackerGeo.Jwt.JwtUtil;
import com.example.GlobalTrackerGeo.Repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

//Dùng CrossOrigin hoặc config trong SecurityConfig để cho phép yêu cầu CORS
//@CrossOrigin(origins = "http://127.0.0.1:5500")// cho phép yêu cầu CORS từ nguồn được chỉ dịnh đến các phương thức class AuthController.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        System.out.println("Receive login request:" + authenticationRequest);
        try {
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()) );
        } catch (Exception e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        //Tìm tài xế đề lấy driverId
        Driver driver = driverRepository.findByEmail(authenticationRequest.getEmail());

        //Tạo jwt
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        LoginResponse loginResponse = new LoginResponse(jwt, driver.getDriverId());
        return ResponseEntity.ok(loginResponse);// trả về đối tượng chứa jwt + driverId
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        System.out.println("Receive signup request:" + signupRequest);

        //Kiểm tra email tồn tại chưa
        if (driverRepository.findByEmail(signupRequest.getEmail()) != null) {
            return "Email already exists!";
        }

        //Tạo đối tượng tài xế (driver) mới
        Driver driver = new Driver();
        driver.setEmail(signupRequest.getEmail());
        //sử dụng PasswordEncoder để mã hóa mật khẩu Driver đăng ký rồi lưu mật khẩu đã mã hóa vào trong PostgreSQL.
        driver.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        driver.setPhone(signupRequest.getPhone());
        driver.setFirstName(signupRequest.getFirstName());
        driver.setLastName(signupRequest.getLastName());

        //Tạo token với thới hạn 3 ngày
        String token = jwtUtil.generateToken(signupRequest.getEmail(), 3);

        driverRepository.save(driver);

        return "Registered successfully!";
    }

}


























