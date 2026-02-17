package com.routing.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routing.dto.Country;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Client responsible for fetching country data from the external API using WebClient.
 *
 * WebClient is the modern, non-blocking, reactive HTTP client recommended by Spring
 * as a replacement for RestTemplate.
 *
 * Follows Single Responsibility Principle (SRP) by handling only
 * the external data retrieval concern.
 */
@Slf4j
@Component
public class CountryDataClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String countriesApiUrl;

    // Timeout configuration
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Constructor with dependency injection.
     *
     * @param webClientBuilder Builder for creating WebClient instances
     * @param objectMapper Jackson ObjectMapper for JSON parsing
     * @param countriesApiUrl URL of the countries API (from application properties)
     */
    public CountryDataClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${countries.api.url:https://raw.githubusercontent.com/mledoze/countries/master/countries.json}")
            String countriesApiUrl) {

        this.countriesApiUrl = countriesApiUrl;
        this.objectMapper = objectMapper;

        // Build WebClient with custom configuration
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024)) // 20MB buffer for large JSON
                .build();

        log.info("CountryDataClient initialized with URL: {}", countriesApiUrl);
    }

    /**
     * Fetches the complete list of countries from the external API.
     *
     * Uses WebClient with reactive programming but blocks to get the result
     * since we need the data synchronously during application startup.
     *
     * @return list of Country objects containing cca3 codes and borders
     * @throws RuntimeException if the API call fails
     */
    public List<Country> fetchCountries() {
        log.info("Fetching country data from: {}", countriesApiUrl);

        try {
            String jsonResponse = webClient
                    .get()
                    .uri(countriesApiUrl)
                    .retrieve()
                    .bodyToMono(String.class)  // Get as String first
                    .timeout(REQUEST_TIMEOUT)
                    .block(RESPONSE_TIMEOUT);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.error("Received empty response from API");
                throw new RuntimeException("No country data received from external API");
            }


            List<Country> countries = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<Country>>() {}
            );

            if (countries == null || countries.isEmpty()) {
                log.error("Received empty or null country list from API");
                throw new RuntimeException("No country data received from external API");
            }

            log.info("Successfully fetched {} countries", countries.size());
            return countries;

        } catch (WebClientResponseException ex) {
            log.error("HTTP Error fetching countries: Status={}, Body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException(
                    String.format("Failed to fetch country data. HTTP Status: %s", ex.getStatusCode()),
                    ex
            );
        } catch (Exception ex) {
            log.error("Fatal error fetching country data: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch country data from external API", ex);
        }
    }

    /**
     * Fetches countries asynchronously (reactive approach).
     *
     * This method returns a Mono for truly reactive, non-blocking operations.
     * Can be used if the application needs to handle data loading asynchronously.
     *
     * @return Mono emitting the list of countries
     */
    public Mono<List<Country>> fetchCountriesAsync() {
        log.info("Fetching country data asynchronously from: {}", countriesApiUrl);

        return webClient
                .get()
                .uri(countriesApiUrl)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .flatMap(jsonResponse -> {
                    try {
                        List<Country> countries = objectMapper.readValue(
                                jsonResponse,
                                new TypeReference<List<Country>>() {}
                        );
                        return Mono.just(countries);
                    } catch (Exception ex) {
                        return Mono.error(new RuntimeException("Failed to parse country data", ex));
                    }
                })
                .doOnSuccess(countries ->
                        log.info("Successfully fetched {} countries asynchronously",
                                countries != null ? countries.size() : 0))
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("HTTP Error fetching countries: Status={}, Body={}",
                                ex.getStatusCode(), ex.getResponseBodyAsString()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    String errorMsg = String.format(
                            "Failed to fetch country data. HTTP Status: %s",
                            ex.getStatusCode()
                    );
                    return Mono.error(new RuntimeException(errorMsg, ex));
                })
                .onErrorResume(Exception.class, ex -> {
                    String errorMsg = "Failed to fetch country data from external API";
                    return Mono.error(new RuntimeException(errorMsg, ex));
                });
    }
}