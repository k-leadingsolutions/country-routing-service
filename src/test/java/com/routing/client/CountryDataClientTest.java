package com.routing.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routing.dto.Country;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CountryDataClient using MockWebServer.
 *
 * Tests the WebClient integration with mocked HTTP responses.
 */
@DisplayName("Country Data Client Tests")
class CountryDataClientTest {

    private MockWebServer mockWebServer;
    private CountryDataClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient.Builder webClientBuilder = WebClient.builder();
        objectMapper = new ObjectMapper();

        client = new CountryDataClient(
                webClientBuilder,
                objectMapper,
                baseUrl + "countries.json"
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should successfully fetch countries from API")
    void testFetchCountriesSuccess() {
        String jsonResponse = """
                [
                    {
                        "cca3": "USA",
                        "borders": ["CAN", "MEX"]
                    },
                    {
                        "cca3": "CAN",
                        "borders": ["USA"]
                    },
                    {
                        "cca3": "MEX",
                        "borders": ["USA", "GTM", "BLZ"]
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));


        List<Country> countries = client.fetchCountries();

        assertNotNull(countries);
        assertEquals(3, countries.size());

        Country usa = countries.stream()
                .filter(c -> "USA".equals(c.getCca3()))
                .findFirst()
                .orElse(null);

        assertNotNull(usa);
        assertEquals("USA", usa.getCca3());
        assertEquals(2, usa.getBorders().size());
        assertTrue(usa.getBorders().contains("CAN"));
        assertTrue(usa.getBorders().contains("MEX"));
    }

    @Test
    @DisplayName("Should handle countries with no borders")
    void testFetchCountriesWithNoBorders() {
        String jsonResponse = """
                [
                    {
                        "cca3": "AUS",
                        "borders": []
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<Country> countries = client.fetchCountries();

        assertNotNull(countries);
        assertEquals(1, countries.size());
        assertEquals("AUS", countries.get(0).getCca3());
        assertTrue(countries.get(0).getBorders().isEmpty());
    }

    @Test
    @DisplayName("Should handle countries with null borders field")
    void testFetchCountriesWithNullBorders() {
        // Arrange
        String jsonResponse = """
                [
                    {
                        "cca3": "ISL"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<Country> countries = client.fetchCountries();

        assertNotNull(countries);
        assertEquals(1, countries.size());
        assertEquals("ISL", countries.get(0).getCca3());
        assertNull(countries.get(0).getBorders());
    }

    @Test
    @DisplayName("Should throw exception when API returns error")
    void testFetchCountriesApiError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> client.fetchCountries()
        );

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw exception when API returns 404")
    void testFetchCountriesNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> client.fetchCountries()
        );

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw exception when API returns empty response")
    void testFetchCountriesEmptyResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> client.fetchCountries()
        );

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void testFetchCountriesMalformedJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{ invalid json")
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> client.fetchCountries()
        );

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should handle empty string response")
    void testFetchCountriesEmptyString() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> client.fetchCountries()
        );

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should successfully fetch countries asynchronously")
    void testFetchCountriesAsync() {
        String jsonResponse = """
                [
                    {
                        "cca3": "USA",
                        "borders": ["CAN", "MEX"]
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Mono<List<Country>> countriesMono = client.fetchCountriesAsync();

        StepVerifier.create(countriesMono)
                .assertNext(countries -> {
                    assertNotNull(countries);
                    assertEquals(1, countries.size());
                    assertEquals("USA", countries.get(0).getCca3());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle error in async fetch")
    void testFetchCountriesAsyncError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        Mono<List<Country>> countriesMono = client.fetchCountriesAsync();

        StepVerifier.create(countriesMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle malformed JSON in async fetch")
    void testFetchCountriesAsyncMalformedJson() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("{ invalid json")
                .addHeader("Content-Type", "application/json"));

        // Act
        Mono<List<Country>> countriesMono = client.fetchCountriesAsync();

        // Assert - Just check that it throws RuntimeException
        StepVerifier.create(countriesMono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should handle large JSON response")
    void testFetchCountriesLargeResponse() {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < 250; i++) {
            if (i > 0) jsonBuilder.append(",");
            jsonBuilder.append(String.format(
                    """
                    {
                        "cca3": "C%02d",
                        "borders": ["C%02d", "C%02d"]
                    }
                    """, i, (i + 1) % 250, (i + 2) % 250
            ));
        }
        jsonBuilder.append("]");

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonBuilder.toString())
                .addHeader("Content-Type", "application/json"));

        List<Country> countries = client.fetchCountries();

        assertNotNull(countries);
        assertEquals(250, countries.size());
    }
}