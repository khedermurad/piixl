package com.piixl.auth_service.controller;

import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;



import java.util.Map;

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



}
