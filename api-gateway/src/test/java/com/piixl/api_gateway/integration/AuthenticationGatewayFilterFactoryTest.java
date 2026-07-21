package com.piixl.api_gateway.integration;



import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.secret=defaultSecretForTesting12345678901234567890",
                "eureka.client.enabled=false",

                "spring.cloud.discovery.client.simple.instances.AUTH-SERVICE[0].uri=http://localhost:${wiremock.server.port}"
        }
)
@AutoConfigureWireMock(port = 0)
public class AuthenticationGatewayFilterFactoryTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void clearWireMock() {
        reset();
    }

    @Test
    void shouldAllowPublicAccessToLoginWithoutToken() {
        stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Login successful")));

        webTestClient.post()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Login successful");

        verify(1, postRequestedFor(urlEqualTo("/api/auth/login")));
    }

    @Test
    void shouldAllowPublicAccessToRegisterWithoutToken() {
        stubFor(post(urlEqualTo("/api/auth/register"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Register successful")));

        webTestClient.post()
                .uri("/api/auth/register")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Register successful");

        verify(1, postRequestedFor(urlEqualTo("/api/auth/register")));
    }

    @Test
    void shouldAllowPublicAccessToLoginEvenWithInvalidToken() {
        stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse().withStatus(200).withBody("Login successful")));

        webTestClient.post()
                .uri("/api/auth/login")
                .cookie("auth_token", "garbage")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthTokenCookieIsMissing(){
        webTestClient.get()
                .uri("/api/auth/me")
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    void shouldReturnUnauthorizedWhenAuthTokenCookieIsEmpty(){
        webTestClient.get()
                .uri("/api/auth/me")
                .cookie("auth_token", "")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedWhenInvalidToken(){
        webTestClient.get()
                .uri("/api/auth/me")
                .cookie("auth_token", "this.is.an.invalid.jwt.token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldRouteRequestAndSetHeadersWhenTokenIsValid() {
        String validToken = generateToken("TestUser", "USER", "12345", 3600000);


        stubFor(get(urlEqualTo("/api/auth/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Success from Auth-Service")));

        webTestClient.get()
                .uri("/api/auth/me")
                .cookie("auth_token", validToken)
                .exchange()
                .expectStatus().isOk();


        verify(1, getRequestedFor(urlEqualTo("/api/auth/me"))
                .withHeader("X-User-Id", equalTo("12345"))
                .withHeader("X-User-Role", equalTo("USER"))
                .withHeader("X-User-Name", equalTo("TestUser")));
    }


    @Test
    void shouldOverwritePreFilledHeadersWithTokenClaims() {
        String validToken = generateToken("TestUser", "USER", "12345", 3600000);


        stubFor(get(urlEqualTo("/api/auth/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Success from Auth-Service")));

        webTestClient.get()
                .uri("/api/auth/me")
                .cookie("auth_token", validToken)
                .header("X-User-Id", "54321")
                .header("X-User-Role", "ADMIN")
                .header("X-User-Name", "TestAdmin")
                .exchange()
                .expectStatus().isOk();


        verify(1, getRequestedFor(urlEqualTo("/api/auth/me"))
                .withHeader("X-User-Id", equalTo("12345"))
                .withHeader("X-User-Role", equalTo("USER"))
                .withHeader("X-User-Name", equalTo("TestUser")));
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
                .signWith(
                        Keys.hmacShaKeyFor("defaultSecretForTesting12345678901234567890"
                                .getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

}
