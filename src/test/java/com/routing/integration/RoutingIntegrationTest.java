package com.routing.integration;

import com.routing.dto.RouteResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    @DisplayName("Integration: Should return valid route from CZE to ITA")
    void testValidRoute() {
        ResponseEntity<RouteResponse> response = restTemplate.getForEntity(
            "/routing/CZE/ITA",
            RouteResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRoute()).containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Should return 400 for island nations")
    void testNoRoute() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/routing/USA/AUS",
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}