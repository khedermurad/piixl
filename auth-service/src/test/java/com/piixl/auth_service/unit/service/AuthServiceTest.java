package com.piixl.auth_service.unit.service;

import com.piixl.auth_service.exception.PasswordMismatchException;
import com.piixl.auth_service.exception.TooYoungException;
import com.piixl.auth_service.exception.UserExistsException;
import com.piixl.auth_service.model.*;
import com.piixl.auth_service.repository.UserRepository;
import com.piixl.auth_service.security.JwtUtil;
import com.piixl.auth_service.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository authRepository;

    // No Mock because of blocking by Mockito
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp(){
        authService = new AuthService(authRepository, passwordEncoder,
                authenticationManager, jwtUtil, rabbitTemplate);
    }

    @Test
    void shouldReturnRegisterResponse(){
        RegisterRequest registerRequest = validRegisterRequest();

        when(authRepository.save(any(UserEntity.class))).thenReturn(UserEntity.builder().id(12L).username("testuser").build());

        RegisterResponse registerResponse = authService.registerUser(registerRequest);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(authRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();
        Assertions.assertNotEquals("test12345", savedUser.getPassword());

        Assertions.assertEquals("test", registerResponse.getUsername());
        Assertions.assertEquals("test@test.com", registerResponse.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenAccountWithUsernameExists(){
        RegisterRequest registerRequest = validRegisterRequest();

        when(authRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        UserExistsException ex = assertThrows(UserExistsException.class, () -> authService.registerUser(registerRequest));
        Assertions.assertEquals("An account with this username already exists: " + registerRequest.getUsername(), ex.getMessage());
        verify(authRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountWithEmailExists(){
        RegisterRequest registerRequest = validRegisterRequest();

        when(authRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        UserExistsException ex = assertThrows(UserExistsException.class, () -> authService.registerUser(registerRequest));
        Assertions.assertEquals("An account with this email already exists: "
                + registerRequest.getEmail(), ex.getMessage());
        verify(authRepository, never()).save(any(UserEntity.class));
    }


    @Test
    void shouldThrowExceptionWhenPasswordMismatch(){
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setPassword("test");
        registerRequest.setPasswordConfirm("different");

        when(authRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(authRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);


        PasswordMismatchException ex = assertThrows(PasswordMismatchException.class, () -> authService.registerUser(registerRequest));
        Assertions.assertEquals("The password entered and the confirmed password do not match.", ex.getMessage());
        verify(authRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenAgeIsBelowExpected(){
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setDateOfBirth(LocalDate.now().minusYears(12));

        when(authRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(authRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);


        TooYoungException ex = assertThrows(TooYoungException.class, () -> authService.registerUser(registerRequest));
        Assertions.assertEquals("You are too young: " + registerRequest.getDateOfBirth(), ex.getMessage());
        verify(authRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void shouldLogin() {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        UserEntity mockUser = UserEntity.builder()
                .id(12L)
                .username("user")
                .role(Role.USER)
                .build();

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication mockAuth = new UsernamePasswordAuthenticationToken("user", "password", authorities);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuth);
        when(authRepository.findByUsername("user")).thenReturn(java.util.Optional.of(mockUser));
        when(jwtUtil.generateToken("user", Role.USER, 12L)).thenReturn("jwt_token");

        String jwtToken = authService.login(loginRequest);

        assertEquals("jwt_token", jwtToken);
        verify(authRepository).findByUsername("user");
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid(){
        LoginRequest loginRequest = new LoginRequest("user", "password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), eq(12L));
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenUserNotFoundAfterAuthentication(){
        LoginRequest loginRequest = new LoginRequest("user", "password");
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication mockAuth = new UsernamePasswordAuthenticationToken("user", "password", authorities);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuth);
        when(authRepository.findByUsername(mockAuth.getName())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), eq(12L));
    }


    static RegisterRequest validRegisterRequest(){
        return RegisterRequest.builder()
                .username("test")
                .email("test@test.com")
                .password("test12345")
                .passwordConfirm("test12345")
                .dateOfBirth(LocalDate.of(1998, 2,2))
                .termsAccepted(true)
                .build();
    }

}
