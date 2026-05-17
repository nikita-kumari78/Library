package com.lms.controller;

import com.lms.dto.AuthRequest;
import com.lms.dto.AuthResponse;
import com.lms.dto.RegisterRequest;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles login and registration.
 *
 * POST /api/auth/login    → returns JWT token
 * POST /api/auth/register → creates new student account
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Login — validate credentials, return JWT.
     *
     * Request:  { "email": "admin@library.com", "password": "admin123" }
     * Response: { "token": "eyJhbGci...", "role": "ROLE_ADMIN", "name": "Admin" }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Spring Security validates credentials here — throws exception if wrong
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()
            )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole().name())
            .build());
    }

    /**
     * Register new student.
     *
     * Request:  { "name": "Rahul", "email": "rahul@college.edu",
     *             "password": "pass123", "department": "CS 2021" }
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.ROLE_STUDENT);
        user.setDepartment(request.getDepartment());
        userRepository.save(user);

        return ResponseEntity.ok("Registration successful");
    }
}
