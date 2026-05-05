package com.piixl.auth_service.unit.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.piixl.auth_service.controller.AuthController;
import com.piixl.auth_service.exception.GlobalExceptionHandler;
import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldReturnRegisterResponseWhenUserRegistersSuccessfully() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        String requestString = objectMapper.writeValueAsString(registerRequest);
        RegisterResponse registerResponse =
                new RegisterResponse(registerRequest.getEmail(), registerRequest.getUsername());

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/auth/register")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(registerRequest.getEmail()))
                .andExpect(jsonPath("$.username").value(registerRequest.getUsername()));
    }

    @Test
    void shouldReturnBadRequestWhenBlankInputRequest() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setUsername("");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidUsernameRequest() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setUsername("test");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEmailRequest() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setEmail("invalid.email.com");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidPasswordRequest() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setPassword("pass1234");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidDateOfBirthRequest() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setDateOfBirth(LocalDate.now());
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnBadRequestWhenInvalidTermsAcceptedRequest() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setTermsAccepted(false);
        String requestString = objectMapper.writeValueAsString(registerRequest);

        performPostRequestAndExpectBadRequest(requestString);
    }

    @Test
    void shouldReturnOkWhenLogin() throws Exception{
        LoginRequest loginRequest = new LoginRequest("test12345", "test12345");
        String requestString = objectMapper.writeValueAsString(loginRequest);

        when(authService.login(any(LoginRequest.class))).thenReturn("jwt_token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt_token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsWrong() throws Exception{
        LoginRequest loginRequest = new LoginRequest("test12345", "test12345");
        String requestString = objectMapper.writeValueAsString(loginRequest);

        when(authService.login(any())).thenThrow(new BadCredentialsException("Wrong Credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isUnauthorized());
    }


    static RegisterRequest validRegisterRequest(){
        return RegisterRequest.builder()
                .username("test12")
                .email("test@test.com")
                .password("Test12345#")
                .passwordConfirm("Test12345#")
                .dateOfBirth(LocalDate.of(1998, 2,2))
                .termsAccepted(true)
                .build();
    }

    void performPostRequestAndExpectBadRequest(String requestString) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isBadRequest());
        verify(authService, never()).registerUser(any(RegisterRequest.class));
    }


}
