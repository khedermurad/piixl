package com.piixl.api_gateway.unit;


import com.piixl.api_gateway.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    private String SECRET =  "mySuperSecretKeyThatIsAtLeast32BytesLong";

    private SecretKey testKey;


    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);

        jwtUtil.init();

        testKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldValidateAndReturnValidClaims(){
        String validToken = generateToken("testUser", "USER", "1001", 360000);

        Claims claims = jwtUtil.validateAndGetClaims(validToken);

        Assertions.assertEquals("testUser", claims.getSubject());
        Assertions.assertEquals("USER", claims.get("role"));
        Assertions.assertEquals("1001", claims.get("userId"));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired(){
        String invalidToken = generateToken("testUser", "USER", "1001", -1000);
        assertThrows(JwtException.class, () -> jwtUtil.validateAndGetClaims(invalidToken));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsManipulated(){
        String validToken = generateToken("testUser", "USER", "1001", 360000);
        String tamperedToken = validToken + "x";
        assertThrows(JwtException.class, () -> jwtUtil.validateAndGetClaims(tamperedToken));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsEmpty(){
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.validateAndGetClaims(""));
    }


    public String generateToken(String username, String role, String userId, int expirationMs){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(testKey)
                .compact();
    }

}
