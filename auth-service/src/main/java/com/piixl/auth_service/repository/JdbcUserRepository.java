package com.piixl.auth_service.repository;

import com.piixl.auth_service.model.Role;
import com.piixl.auth_service.model.UserEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository{

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate){
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        String sql = "INSERT INTO users (created_at, email, enabled, password, " +
                "role, terms_accepted, username, date_of_birth) VALUES " +
                "(:created_at, :email::email_address, :enabled, :password, :role::user_role, " +
                ":terms_accepted, :username, :date_of_birth)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("created_at", userEntity.getCreatedAt());
        params.addValue("email", userEntity.getEmail());
        params.addValue("enabled", userEntity.getEnabled());
        params.addValue("password", userEntity.getPassword());
        params.addValue("role", userEntity.getRole() != null ? userEntity.getRole().toString() : "USER");
        params.addValue("terms_accepted", userEntity.getTermsAccepted());
        params.addValue("username", userEntity.getUsername());
        params.addValue("date_of_birth", userEntity.getDateOfBirth());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        if(keyHolder.getKey() != null){
            userEntity.setId(keyHolder.getKey().longValue());
        }

        return userEntity;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = :username";
        Map<String, String> params = Map.of("username", username);
        List<UserEntity> results = namedParameterJdbcTemplate.query(sql, params, USER_ROW_MAPPER);

        return results.stream().findFirst();
    }

    @Override
    public Boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = :username";
        Map<String, String> params = Map.of("username", username);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);

        return count != null && count > 0;
    }

    @Override
    public Boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";
        Map<String, String> params = Map.of("email", email);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);

        return count != null && count > 0;
    }

    @Override
    public void deleteAllInBatch() {
        String sql = "DELETE FROM users";
        namedParameterJdbcTemplate.getJdbcOperations().update(sql);
    }

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT * FROM users";

        return namedParameterJdbcTemplate.getJdbcOperations().query(sql, USER_ROW_MAPPER);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = :id";
        Map<String, Long> params = Map.of("id", id);
        List<UserEntity> results = namedParameterJdbcTemplate.query(sql, params, USER_ROW_MAPPER);

        return results.stream().findFirst();
    }

    private static final RowMapper<UserEntity> USER_ROW_MAPPER = (rs, rowNum) ->
            UserEntity.builder()
                    .id(rs.getLong("id"))
                    .createdAt(rs.getObject("created_at", LocalDate.class))
                    .email(rs.getString("email"))
                    .enabled(rs.getBoolean("enabled"))
                    .password(rs.getString("password"))
                    .role(Role.valueOf(rs.getString("role")))
                    .termsAccepted(rs.getBoolean("terms_accepted"))
                    .username(rs.getString("username"))
                    .dateOfBirth(rs.getObject("date_of_birth", LocalDate.class))
                    .build();

}
