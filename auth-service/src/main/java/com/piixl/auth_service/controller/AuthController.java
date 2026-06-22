package com.piixl.auth_service.controller;

import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        String jwtToken = this.authService.login(loginRequest);

        Cookie cookie = new Cookie("auth_token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtExpiration);
        response.addCookie(cookie);


        return ResponseEntity.ok("Login successful");
    }
}
