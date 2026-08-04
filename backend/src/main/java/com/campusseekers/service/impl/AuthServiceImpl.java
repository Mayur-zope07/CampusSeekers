package com.campusseekers.service.impl;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CurrentUserResponse;
import com.campusseekers.dto.LoginRequest;
import com.campusseekers.dto.LoginResponse;
import com.campusseekers.dto.RegisterRequest;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.exception.UnauthorizedException;
import com.campusseekers.mapper.AuthMapper;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.security.jwt.JwtService;
import com.campusseekers.service.AuthService;
import com.campusseekers.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Override
    @Transactional
    public ApiResponse<Void> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Registration successful")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Add custom claims to the JWT payload
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId().toString());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(extraClaims, user);

        LoginResponse response = authMapper.toLoginResponse(user);
        response.setAccessToken(token);
        // Generate UUID placeholder for future refresh token support
        response.setRefreshToken(UUID.randomUUID().toString());
        response.setExpiresIn(jwtExpiration / 1000); // Expiration time in seconds

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user session found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return authMapper.toCurrentUserResponse(user);
    }
}
