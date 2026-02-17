package com.routing;

import com.routing.service.impl.CountryDataService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.*;

/**
 * Test configuration that provides a mock CountryDataService
 * to avoid real HTTP calls during integration tests.
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Creates a test CountryDataService with pre-loaded test data.
     * This bypasses the @PostConstruct initialization that would call the real API.
     */
    @Bean
    @Primary
    public CountryDataService testCountryDataService() {
        return new TestCountryDataService();
    }
    
    /**
     * Test implementation of CountryDataService with hardcoded test data.
     */
    public static class TestCountryDataService extends CountryDataService {
        
        private final Map<String, Set<String>> testAdjacencyMap;
        
        public TestCountryDataService() {
            super(null); // Pass null since we won't use the real client
            this.testAdjacencyMap = createTestAdjacencyMap();
        }
        
        @Override
        public void initialize() {
            // Override to prevent @PostConstruct from running
            // Data is already loaded in constructor
        }
        
        @Override
        public Map<String, Set<String>> getAdjacencyMap() {
            return Collections.unmodifiableMap(testAdjacencyMap);
        }
        
        @Override
        public boolean countryExists(String cca3) {
            return testAdjacencyMap.containsKey(cca3);
        }
        
        /**
         * Creates test adjacency map with sample countries for testing.
         */
        private Map<String, Set<String>> createTestAdjacencyMap() {
            Map<String, Set<String>> map = new HashMap<>();
            
            // Czech Republic
            map.put("CZE", Set.of("AUT", "DEU", "POL", "SVK"));
            
            // Austria
            map.put("AUT", Set.of("CZE", "DEU", "HUN", "ITA", "SVN", "SVK", "CHE", "LIE"));
            
            // Italy
            map.put("ITA", Set.of("AUT", "CHE", "FRA", "SMR", "SVN", "VAT"));
            
            // USA
            map.put("USA", Set.of("CAN", "MEX"));
            
            // Canada
            map.put("CAN", Set.of("USA"));
            
            // Mexico
            map.put("MEX", Set.of("USA", "GTM", "BLZ"));
            
            // Australia (island - no land borders)
            map.put("AUS", Set.of());
            
            // Portugal
            map.put("PRT", Set.of("ESP"));
            
            // Spain
            map.put("ESP", Set.of("PRT", "FRA", "AND"));
            
            // France
            map.put("FRA", Set.of("ESP", "ITA", "CHE", "DEU", "BEL", "LUX", "AND", "MCO"));
            
            // Germany
            map.put("DEU", Set.of("FRA", "CHE", "AUT", "CZE", "POL", "DNK", "NLD", "BEL", "LUX"));
            
            // Poland
            map.put("POL", Set.of("DEU", "CZE", "SVK", "UKR", "BLR", "LTU", "RUS"));
            
            // Ukraine
            map.put("UKR", Set.of("POL", "SVK", "HUN", "ROU", "MDA", "BLR", "RUS"));
            
            // Slovakia
            map.put("SVK", Set.of("POL", "CZE", "AUT", "HUN", "UKR"));
            
            // Hungary
            map.put("HUN", Set.of("AUT", "SVK", "UKR", "ROU", "SRB", "HRV", "SVN"));
            
            // Slovenia
            map.put("SVN", Set.of("ITA", "AUT", "HUN", "HRV"));
            
            // Switzerland
            map.put("CHE", Set.of("FRA", "ITA", "AUT", "DEU", "LIE"));
            
            return map;
        }
    }
}