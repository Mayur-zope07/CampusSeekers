package com.campusseekers.service;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.LoginRequest;
import com.campusseekers.dto.LoginResponse;
import com.campusseekers.dto.RegisterRequest;
import com.campusseekers.entity.Role;
import com.campusseekers.entity.User;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.mapper.AuthMapper;
import com.campusseekers.repository.UserRepository;
import com.campusseekers.security.jwt.JwtService;
import com.campusseekers.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        RegisterRequest request = RegisterRequest.builder()
                .email("student@example.com")
                .password("Password@123")
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashedPassword");

        ApiResponse<Void> response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("student@example.com")
                .password("Password@123")
                .role(Role.STUDENT)
                .build();

        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnResponse_WhenCredentialsAreValid() {
        LoginRequest request = LoginRequest.builder()
                .email("student@example.com")
                .password("Password@123")
                .build();

        User user = User.builder()
                .email("student@example.com")
                .role(Role.STUDENT)
                .build();
        user.setId(UUID.randomUUID());

        LoginResponse loginResponseMock = LoginResponse.builder()
                .role(Role.STUDENT)
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("mockJwtToken");
        when(authMapper.toLoginResponse(user)).thenReturn(loginResponseMock);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals(Role.STUDENT, response.getRole());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        LoginRequest request = LoginRequest.builder()
                .email("student@example.com")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
