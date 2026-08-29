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
import com.piixl.auth_service.repository.UserRepository;
import com.piixl.auth_service.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private RabbitTemplate rabbitTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RabbitTemplate rabbitTemplate){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public RegisterResponse registerUser(RegisterRequest registerRequest){
        if (userRepository.existsByUsername(registerRequest.getUsername().toLowerCase())){
            throw new UserExistsException("An account with this username already exists: "
            + registerRequest.getUsername());
        }
        if(userRepository.existsByEmail(registerRequest.getEmail().toLowerCase())){
            throw new UserExistsException("An account with this email already exists: "
            + registerRequest.getEmail());
        }
        if (!registerRequest.getPassword().equals(registerRequest.getPasswordConfirm())){
            throw new PasswordMismatchException("The password entered and the confirmed password do not match.");
        }
        if(Period.between(registerRequest.getDateOfBirth(), LocalDate.now()).getYears() < 13){
            throw new TooYoungException("You are too young: " + registerRequest.getDateOfBirth());
        }

        UserEntity userEntity = this.userRepository.save(UserEntity
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
        // TODO: Dual-Write risk! Database commit might fail after RabbitMQ message is sent.
        // Needs refactoring to Transactional Outbox Pattern (see Ticket #32)
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

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> {
                    logger.warn("Authenticated user {} not found in repository", authentication.getName());
                    return new BadCredentialsException("Username or password is incorrect");
                });

        String roleName = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        Role userRole = Role.valueOf(roleName.replace("ROLE_", ""));

        return jwtUtil.generateToken(authentication.getName(), userRole, user.getId());
    }

    public Map<String, Boolean> checkUserExistence(String username, String email){
        Map<String, Boolean> result = new HashMap<>();

        result.put("emailExists", StringUtils.hasText(email)
                ? userRepository.existsByEmail(email)
                : null);

        result.put("usernameExists", StringUtils.hasText(username)
                ? userRepository.existsByUsername(username)
                : null);

        return result;
    }


}
