package com.example.GlobalTrackerGeo.Jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Áp dụng cho các request HTTP, thực hiện kiểm tra JWT trong các request HTTP để xác thực.
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;//UseDetailsService là 1 interface => phải có một class implement (CustomUserDetailsService) NẾU KHÔNG SẼ gặp lỗi tạo bean JwtRequestFilter vì trong class này có UserDetailsService - mà hiện nó đang là 1 interface ko phải 1 bean. TÓM LẠI lỗi tạo bean JwtRequestFilter do chưa tạo bean UserDetailsService (gốc là interface)

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //Lấy nội dung của Authorization trong header front-end gửi sang khi request service.
        final String authorizationHeader = request.getHeader("Authorization");//headers: {'Authorization': `Bearer ${jwt1}`}

        String username = null;
        String jwt  = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // Bỏ "Bearer " lấy ${jwt} để lấy Token
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired.");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
