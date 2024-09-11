package com.example.GlobalTrackerGeo.Config;

import com.example.GlobalTrackerGeo.Jwt.JwtRequestFilter;
import com.example.GlobalTrackerGeo.Service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            config.setAllowedOrigins(List.of("*")); //Thêm nguồn front-end
                            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(List.of("*"));
                            return config;
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()// chỉ định đường dẫn + phân quyền
                        .requestMatchers("/api/**")// (tránh lỗi 403 :forbidden) chỉ định các đường dẫn th này là: tất cả URL bắt đầu với /public/. Kí tự ** là đường dẫn con, phần sau (sub-path): dùng cho các tài nguyên công cộng như: trang giới thiệu, hoặc các API ko cần bảo mật
                        .permitAll() // tất cả các request đến các đường dẫn đã chỉ định trong requestMatchers("", "",vv...) ở trên sẽ được phép truy cập mà không cần phải xác thực (Authentication).(dù đăng nhập rồi hay chưa, cũng có thể truy cập các URL này.
                        .anyRequest().authenticated() // Các request khác cần phải xác thực
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)//đảm bảo ứng dụng ko dùng session, (tất cả các request HTTP đều phải có JWT)
                );

        // Thêm bộ lọc JWT vào chuỗi bộ lọc bảo mật
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws  Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}


















































