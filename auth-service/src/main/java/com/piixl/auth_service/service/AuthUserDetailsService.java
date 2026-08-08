package com.piixl.auth_service.service;

import com.piixl.auth_service.model.UserEntity;
import com.piixl.auth_service.repository.JdbcUserRepository;
import com.piixl.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public AuthUserDetailsService(JdbcUserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User
                .withUsername(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRole().toString())
                .build();
    }
}
