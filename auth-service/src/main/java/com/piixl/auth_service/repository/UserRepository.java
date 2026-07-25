package com.piixl.auth_service.repository;

import com.piixl.auth_service.model.UserEntity;

import java.util.Optional;

public interface UserRepository {
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    UserEntity save(UserEntity userEntity);
}
