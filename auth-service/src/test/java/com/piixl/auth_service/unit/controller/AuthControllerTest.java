package com.piixl.auth_service.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.piixl.auth_service.controller.AuthController;
import com.piixl.auth_service.exception.GlobalExceptionHandler;
import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.model.UserExistenceResponse;
import com.piixl.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.time.LocalDate;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

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

        MethodValidationPostProcessor validationPostProcessor = new MethodValidationPostProcessor();
        validationPostProcessor.afterPropertiesSet();

        Object validatedController = validationPostProcessor
                .postProcessAfterInitialization(authController, "authController");

        mockMvc = MockMvcBuilders.standaloneSetup(validatedController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
        String fakeJwtToken = "jwt-token-test";

        when(authService.login(any(LoginRequest.class))).thenReturn(fakeJwtToken);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestString))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"))
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().value("auth_token", fakeJwtToken))
                .andExpect(cookie().httpOnly("auth_token", true))
                .andExpect(cookie().secure("auth_token", true))
                .andExpect(cookie().path("auth_token", "/"))
                .andExpect(cookie().sameSite("auth_token", "Lax"));
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



    @Test
    void shouldReturnOkAndUsernameWhenRequestMe() throws Exception {
        String username = "testUser01";

        mockMvc.perform(get("/api/auth/me")
                        .header("X-User-Name", username)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnOkAndCookieWithAnEmptyJWTAndMaxAgeOfZero() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("auth_token"))
                .andExpect(cookie().value("auth_token", ""))
                .andExpect(cookie().httpOnly("auth_token", true))
                .andExpect(cookie().secure("auth_token", true))
                .andExpect(cookie().path("auth_token", "/"))
                .andExpect(cookie().maxAge("auth_token", 0))
                .andExpect(cookie().sameSite("auth_token", "Lax"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestCheckExistenceWithoutRequestParameter() throws Exception{
        mockMvc.perform(get("/api/auth/check-existence")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCheckExistenceAndUsernameIsNotBetween5And20Letters() throws Exception{
        mockMvc.perform(get("/api/auth/check-existence?username=abcd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCheckExistenceAndUsernameDoesNotStartWith5Letters() throws Exception{
        mockMvc.perform(get("/api/auth/check-existence?username=45651")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCheckExistenceAndEmailIsNotValid() throws Exception{
        mockMvc.perform(get("/api/auth/check-existence?email=thisismyemail")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTrueForEmailWhenCheckExistenceAndEmailIsAvailable() throws Exception{
        UserExistenceResponse result = new UserExistenceResponse(null, true);

        when(authService.checkUserExistence(isNull(), anyString())).thenReturn(result);

        mockMvc.perform(get("/api/auth/check-existence?email=test@email.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailExists", is(true)))
                .andExpect(jsonPath("$.usernameExists", is(nullValue())));
    }

    @Test
    void shouldReturnFalseForUsernameWhenCheckExistenceAndUsernameIsNotAvailable() throws Exception{
        UserExistenceResponse result = new UserExistenceResponse(false, null);

        when(authService.checkUserExistence(anyString(), isNull())).thenReturn(result);

        mockMvc.perform(get("/api/auth/check-existence?username=testUser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailExists", is(nullValue())))
                .andExpect(jsonPath("$.usernameExists", is(false)));
    }


    @Test
    void shouldReturnFalseForUsernameAndEmailWhenCheckExistenceAndUsernameAndEmailAreNotAvailable() throws Exception{
        UserExistenceResponse result = new UserExistenceResponse(false, false);

        when(authService.checkUserExistence(anyString(), anyString())).thenReturn(result);

        mockMvc.perform(get("/api/auth/check-existence?username=testUser&email=test@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailExists", is(false)))
                .andExpect(jsonPath("$.usernameExists", is(false)));
    }


    @Test
    void shouldReturnTrueForUsernameAndEmailWhenCheckExistenceAndUsernameAndEmailAreAvailable() throws Exception{
        UserExistenceResponse result = new UserExistenceResponse(true, true);

        when(authService.checkUserExistence(anyString(), anyString())).thenReturn(result);

        mockMvc.perform(get("/api/auth/check-existence?username=testUser&email=test@test.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailExists", is(true)))
                .andExpect(jsonPath("$.usernameExists", is(true)));
    }




    static RegisterRequest validRegisterRequest(){
        return RegisterRequest.builder()
                .username("testuser12")
                .profileName("T")
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
