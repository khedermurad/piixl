package com.piixl.auth_service.controller;

import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.service.AuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private AuthService authService;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Validated @RequestBody RegisterRequest registerRequest){
        RegisterResponse response =  authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest){
        String jwtToken = this.authService.login(loginRequest);


        ResponseCookie cookie = ResponseCookie.
                from("auth_token", jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtExpiration)
                .sameSite("Lax")
                .build();



        return ResponseEntity.ok().
                header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful");
    }


    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(@RequestHeader("X-User-Name") String username){
        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok().
                header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/check-existence")
    public ResponseEntity<Map<String, Boolean>> checkExistence(
            @RequestParam(required = false)
            @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
            @Pattern(regexp = "^[A-Za-z]{5}.*", message = "Username must start with 5 letters")
            String username,

            @RequestParam(required = false)
            @Email(message = "Email should be valid")
            String email) {

        if (!StringUtils.hasText(username) && !StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must enter either 'username' or 'email'.");
        }

        return ResponseEntity
                .ok()
                .header("Cache-Control", "no-store")
                .body(authService.checkUserExistence(username, email));
    }


}
