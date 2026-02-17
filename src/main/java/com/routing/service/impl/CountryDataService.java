package com.routing.service.impl;

import com.routing.client.CountryDataClient;
import com.routing.dto.Country;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service responsible for loading and caching country border data.
 *
 * Builds an adjacency list at startup for efficient graph traversal.
 * Follows Single Responsibility Principle (SRP) by handling only
 * data loading and caching concerns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CountryDataService {

    private final CountryDataClient countryDataClient;

    /**
     * Adjacency list representation of country borders.
     * Key: cca3 country code
     * Value: Set of neighboring country codes
     */
    private Map<String, Set<String>> adjacencyMap;

    /**
     * Initializes the country data by fetching from external API
     * and building the adjacency list.
     *
     * Executed once at application startup using @PostConstruct.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing country data...");

        List<Country> countries = countryDataClient.fetchCountries();
        adjacencyMap = buildAdjacencyMap(countries);

        log.info("Country data initialized with {} countries", adjacencyMap.size());
    }

    /**
     * Builds an adjacency list from the country data.
     *
     * @param countries list of countries with border information
     * @return adjacency map representing the border graph
     */
    private Map<String, Set<String>> buildAdjacencyMap(List<Country> countries) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Country country : countries) {
            String cca3 = country.getCca3();
            List<String> borders = country.getBorders();

            // Initialize
            map.putIfAbsent(cca3, new HashSet<>());

            // Add borders (handles null safely)
            if (borders != null && !borders.isEmpty()) {
                map.get(cca3).addAll(borders);
            }
        }

        return map;
    }

    /**
     * Gets the adjacency map.
     *
     * @return unmodifiable view of the adjacency map
     */
    public Map<String, Set<String>> getAdjacencyMap() {
        return Collections.unmodifiableMap(adjacencyMap);
    }

    /**
     * Checks if a country exists in the dataset.
     *
     * @param cca3 the country code to check
     * @return true if the country exists
     */
    public boolean countryExists(String cca3) {
        return adjacencyMap.containsKey(cca3);
    }
}