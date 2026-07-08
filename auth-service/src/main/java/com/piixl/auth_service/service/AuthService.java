package com.piixl.auth_service.service;

import com.piixl.auth_service.config.RabbitConfig;
import com.piixl.auth_service.exception.PasswordMismatchException;
import com.piixl.auth_service.exception.TooYoungException;
import com.piixl.auth_service.exception.UserExistsException;
import com.piixl.auth_service.model.RegisterRequest;
import com.piixl.auth_service.model.RegisterResponse;
import com.piixl.auth_service.model.Role;
import com.piixl.auth_service.model.UserEntity;
import com.piixl.auth_service.model.UserEvent;
import com.piixl.auth_service.model.LoginRequest;
import com.piixl.auth_service.repository.AuthRepository;
import com.piixl.auth_service.security.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

@Service
public class AuthService {
    private AuthRepository authRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private RabbitTemplate rabbitTemplate;


    @Autowired
    public AuthService(AuthRepository authRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RabbitTemplate rabbitTemplate){
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    public RegisterResponse registerUser(RegisterRequest registerRequest){
        if (authRepository.existsByUsername(registerRequest.getUsername().toLowerCase())){
            throw new UserExistsException("An account with this username already exists: "
            + registerRequest.getUsername());
        }
        if(authRepository.existsByEmail(registerRequest.getEmail().toLowerCase())){
            throw new UserExistsException("An account with this email already exists: "
            + registerRequest.getEmail());
        }
        if (!registerRequest.getPassword().equals(registerRequest.getPasswordConfirm())){
            throw new PasswordMismatchException("The password entered and the confirmed password do not match.");
        }
        if(Period.between(registerRequest.getDateOfBirth(), LocalDate.now()).getYears() < 13){
            throw new TooYoungException("You are too young: " + registerRequest.getDateOfBirth());
        }

        UserEntity userEntity = this.authRepository.save(UserEntity
                .builder().username(registerRequest.getUsername().toLowerCase())
                .email(registerRequest.getEmail().toLowerCase())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .termsAccepted(registerRequest.getTermsAccepted())
                .enabled(false)
                .role(Role.USER)
                .dateOfBirth(registerRequest.getDateOfBirth())
                .createdAt(LocalDate.now())
                .build());

        UserEvent userEvent = new UserEvent(userEntity.getId(), registerRequest.getProfileName(),
                userEntity.getUsername(), userEntity.getEmail(), userEntity.getDateOfBirth());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "user.created", userEvent);

        return RegisterResponse.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail()).build();
    }

    public String login(LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername().toLowerCase(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserEntity user = authRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("Username or password is incorrect"));

        String roleName = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        Role userRole = Role.valueOf(roleName.replace("ROLE_", ""));

        return jwtUtil.generateToken(authentication.getName(), userRole, user.getId());
    }


}
