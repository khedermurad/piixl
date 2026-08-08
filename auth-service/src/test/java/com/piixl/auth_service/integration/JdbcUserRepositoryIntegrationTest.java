package com.piixl.auth_service.integration;

import com.piixl.auth_service.TestContainersConfiguration;
import com.piixl.auth_service.model.Role;
import com.piixl.auth_service.model.UserEntity;
import com.piixl.auth_service.repository.JdbcUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({TestContainersConfiguration.class, JdbcUserRepository.class, BCryptPasswordEncoder.class})
@ActiveProfiles("test")
@Transactional
public class JdbcUserRepositoryIntegrationTest {

    @Autowired
    private JdbcUserRepository repository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void shouldSaveUserAndFindById(){
        UserEntity user = validUserEntity();

        UserEntity savedUser = repository.save(user);

        Optional<UserEntity> foundUser = repository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.get().getDateOfBirth()).isEqualTo(user.getDateOfBirth());
        assertThat(foundUser.get().getTermsAccepted()).isEqualTo(user.getTermsAccepted());
        assertThat(foundUser.get().getRole()).isEqualTo(user.getRole());
        assertThat(foundUser.get().getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(foundUser.get().getEnabled()).isEqualTo(user.getEnabled());
        assertThat(foundUser.get().getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    void shouldSaveUserAndFindByUsername(){
        UserEntity user = validUserEntity();

        UserEntity savedUser = repository.save(user);

        Optional<UserEntity> foundUser = repository.findByUsername(savedUser.getUsername());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());
        assertThat(foundUser.get().getUsername()).isEqualTo(savedUser.getUsername());
    }


    @Test
    void shouldThrowExceptionWhenUserExists(){
        UserEntity user = validUserEntity();
        user.setPassword(passwordEncoder.encode("Brunoo12345#"));

        repository.save(user);

        assertThatThrownBy(() -> repository.save(user)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists(){
        UserEntity user = validUserEntity();

        repository.save(user);

        UserEntity differentUsernameEqualEmailUser = validUserEntity();
        differentUsernameEqualEmailUser.setUsername("TestTestUser100");
        differentUsernameEqualEmailUser.setEmail(user.getEmail());

        assertThatThrownBy(() -> repository.save(differentUsernameEqualEmailUser)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldReturnTrueWhenUserExists(){
        UserEntity user = validUserEntity();
        repository.save(user);

        Boolean userExistsByUsername = repository.existsByUsername(user.getUsername());
        Boolean userExistsByEmail = repository.existsByEmail(user.getEmail());

        assertThat(userExistsByUsername).isTrue();
        assertThat(userExistsByEmail).isTrue();
    }


    @Test
    void shouldReturnFalseWhenUserDoesNotExists(){
        UserEntity user = validUserEntity();
        repository.save(user);

        Boolean userExistsByEmail = repository.existsByEmail("testuser98@test.com");
        Boolean userExistsByUsername = repository.existsByUsername("testuser98");

        assertThat(userExistsByUsername).isFalse();
        assertThat(userExistsByEmail).isFalse();
    }

    @Test
    void shouldDeleteAllUsers(){
        UserEntity user1 = validUserEntity();
        UserEntity user2 = validUserEntity();
        user2.setUsername("testuser100");
        user2.setEmail("testuser100@test.com");
        UserEntity user3 = validUserEntity();
        user3.setUsername("testuser101");
        user3.setEmail("testuser101@test.com");

        repository.save(user1);
        repository.save(user2);
        repository.save(user3);

        List<UserEntity> savedUserList = repository.findAll();
        assertThat(savedUserList).hasSize(3);

        repository.deleteAllInBatch();

        List<UserEntity> userList = repository.findAll();
        assertThat(userList).isEmpty();
    }

    @Test
    void shouldIgnoreSetIdAndUseDatabaseGeneratedId() {
        UserEntity user = validUserEntity();
        user.setId(456L);

        UserEntity savedUser = repository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isNotEqualTo(456L);
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindByIdNotFound() {
        Optional<UserEntity> foundUser = repository.findById(999L);
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindByUsernameNotFound() {
        Optional<UserEntity> foundUser = repository.findByUsername("unknownUser");
        assertThat(foundUser).isEmpty();
    }


    private UserEntity validUserEntity(){
        return UserEntity.builder()
                .username("testuser99")
                .email("testuser99@test.com")
                .password(passwordEncoder.encode("Brunoo12345#"))
                .dateOfBirth(LocalDate.of(2000,1,1))
                .termsAccepted(true)
                .role(Role.USER)
                .createdAt(LocalDate.now())
                .enabled(false)
                .build();
    }

}
