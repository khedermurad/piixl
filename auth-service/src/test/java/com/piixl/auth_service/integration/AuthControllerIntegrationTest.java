package com.piixl.auth_service.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.piixl.auth_service.TestContainersConfiguration;
import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.UserEntity;
import com.piixl.auth_service.repository.JdbcUserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import({TestContainersConfiguration.class})
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcUserRepository jdbcUserRepository;

    private ObjectMapper objectMapper;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp(){
        SecurityContextHolder.clearContext();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeEach
    void cleanUpDb(){
        jdbcUserRepository.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void shouldRegisterUserInRealDatabase() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Optional<UserEntity> savedUser = jdbcUserRepository.findByUsername(registerRequest.getUsername());
        List<UserEntity> userList = jdbcUserRepository.findAll();

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(savedUser.get().getPassword()).isNotEqualTo(registerRequest.getPassword());
        assertThat(userList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnConflictWhenUserWithEqualUsernameExists() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());


        assertThat(jdbcUserRepository.findByUsername(registerRequest.getUsername()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo(registerRequest.getEmail());
                    assertThat(user.getUsername()).isEqualTo(registerRequest.getUsername());
                });

        registerRequest.setUsername("testUser12");
        registerRequest.setEmail("test2@test.com");

        requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this username already exists: "
                        + registerRequest.getUsername()));
    }

    @Test
    void shouldReturnConflictWhenUserWithEqualEmailExists() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setEmail("test@test.com");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(jdbcUserRepository.findByUsername(registerRequest.getUsername()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo(registerRequest.getEmail());
                    assertThat(user.getUsername()).isEqualTo(registerRequest.getUsername());
                });

        registerRequest.setUsername("newusername");
        registerRequest.setEmail("test@TEST.com");

        requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists: "
                        + registerRequest.getEmail()));
    }


    @Test
    void shouldReturnBadRequestWhenInvalidInput() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setEmail("invalid.email.com");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThat(jdbcUserRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldReturnUnprocessableEntityWhenPasswordMismatch() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setPassword("Test12345#");
        registerRequest.setPasswordConfirm("Test12345#test");
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("The password entered and the confirmed password do not match."));

        assertThat(jdbcUserRepository.findAll().size()).isEqualTo(0);
    }


    @Test
    void shouldReturnUnprocessableEntityWhenAgeNotValid() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        registerRequest.setDateOfBirth(LocalDate.now().minusYears(12));
        String requestString = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("You are too young: "
                + registerRequest.getDateOfBirth()));
        assertThat(jdbcUserRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldReturnOkWhenLogin() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(),
                registerRequest.getPassword());
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .content(loginString)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginUsernameDoesNotExists() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(),
                "testpassword");
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Username or password is incorrect"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginInvalidPassword() throws Exception{
        LoginRequest loginRequest = new LoginRequest("username", "password");
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Username or password is incorrect"));
    }


    @Test
    void shouldReturnOkWhenLoginUsernameIsUppercase() throws Exception{
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest
                .getUsername().toUpperCase(), registerRequest.getPassword());
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }


    @Test
    void shouldReturnOkWhenValidTokenProvided() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(),
                registerRequest.getPassword());
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(cookie().exists("auth_token"))
                                .andReturn();

        Cookie cookie = result.getResponse().getCookie("auth_token");


        mockMvc.perform(get("/api/test/protected")
                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().string("Access granted"));
    }

    @Test
    void shouldReturnOkAndUsernameWhenRequestMe() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(),
                registerRequest.getPassword());
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult mvcResult =  mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful")).andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("auth_token");

        mockMvc.perform(get("/api/auth/me")
                        .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", loginRequest.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(loginRequest.getUsername()));
    }


    @Test
    void shouldReturnOkAndCookieWithAnEmptyJWTAndMaxAgeOfZeroWhenLogout() throws Exception {
        RegisterRequest registerRequest = validRegisterRequest();
        String registerString = objectMapper.writeValueAsString(registerRequest);

        LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(),
                registerRequest.getPassword());
        String loginString = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/auth/register")
                        .content(registerString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        MvcResult mvcResult =  mockMvc.perform(post("/api/auth/login")
                        .content(loginString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful")).andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("auth_token");

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Cookie logoutCookie = logoutResult.getResponse().getCookie("auth_token");

        assertThat(logoutCookie).isNotNull();
        assertThat(logoutCookie.getName()).isEqualTo("auth_token");
        assertThat(logoutCookie.getValue()).isEqualTo("");
        assertThat(logoutCookie.isHttpOnly()).isTrue();
        assertThat(logoutCookie.getSecure()).isTrue();
        assertThat(logoutCookie.getPath()).isEqualTo("/");
        assertThat(logoutCookie.getMaxAge()).isEqualTo(0);
        assertThat(logoutCookie.getAttribute("SameSite")).isEqualTo("Lax");
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


}
