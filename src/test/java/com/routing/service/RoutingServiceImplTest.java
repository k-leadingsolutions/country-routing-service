package com.routing.service;

import com.routing.exception.NoRouteFoundException;
import com.routing.monitoring.RoutingMetrics;
import com.routing.service.impl.CountryDataService;
import com.routing.service.impl.RoutingServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BfsRoutingService.
 *
 * Tests cover various scenarios including:
 * - Direct routes (neighbors)
 * - Multi-hop routes
 * - Same origin and destination
 * - No route available (islands)
 * - Invalid countries
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BFS Routing Service Tests")
class RoutingServiceImplTest {

    @Mock
    private CountryDataService countryDataService;

    private RoutingMetrics routingMetrics;
    private RoutingServiceImpl routingService;

    @BeforeEach
    void setUp() {
        // Create a real RoutingMetrics with SimpleMeterRegistry for testing
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        routingMetrics = new RoutingMetrics(meterRegistry);

        routingService = new RoutingServiceImpl(countryDataService, routingMetrics);
    }

    @Test
    @DisplayName("Should find direct route between neighboring countries")
    void testDirectRoute() {
        Map<String, Set<String>> adjacencyMap = new HashMap<>();
        adjacencyMap.put("USA", Set.of("CAN", "MEX"));
        adjacencyMap.put("CAN", Set.of("USA"));
        adjacencyMap.put("MEX", Set.of("USA"));

        when(countryDataService.getAdjacencyMap()).thenReturn(adjacencyMap);
        when(countryDataService.countryExists("USA")).thenReturn(true);
        when(countryDataService.countryExists("CAN")).thenReturn(true);


        List<String> route = routingService.calculateRoute("USA", "CAN");

        assertNotNull(route);
        assertEquals(2, route.size());
        assertEquals("USA", route.get(0));
        assertEquals("CAN", route.get(1));

        verify(countryDataService).countryExists("USA");
        verify(countryDataService).countryExists("CAN");
        verify(countryDataService).getAdjacencyMap();
    }

    @Test
    @DisplayName("Should find shortest route with multiple hops")
    void testMultiHopRoute() {

        Map<String, Set<String>> adjacencyMap = new HashMap<>();
        adjacencyMap.put("CZE", Set.of("AUT", "DEU", "POL", "SVK"));
        adjacencyMap.put("AUT", Set.of("CZE", "DEU", "HUN", "ITA", "SVN", "SVK", "CHE", "LIE"));
        adjacencyMap.put("ITA", Set.of("AUT", "CHE", "FRA", "SMR", "SVN", "VAT"));
        adjacencyMap.put("DEU", Set.of("AUT", "BEL", "CZE", "DNK", "FRA", "LUX", "NLD", "POL", "CHE"));
        adjacencyMap.put("POL", Set.of("BLR", "CZE", "DEU", "LTU", "RUS", "SVK", "UKR"));
        adjacencyMap.put("SVK", Set.of("AUT", "CZE", "HUN", "POL", "UKR"));
        adjacencyMap.put("HUN", Set.of("AUT", "HRV", "ROU", "SRB", "SVK", "SVN", "UKR"));
        adjacencyMap.put("SVN", Set.of("AUT", "HRV", "HUN", "ITA"));
        adjacencyMap.put("CHE", Set.of("AUT", "FRA", "ITA", "LIE", "DEU"));
        adjacencyMap.put("LIE", Set.of("AUT", "CHE"));

        when(countryDataService.getAdjacencyMap()).thenReturn(adjacencyMap);
        when(countryDataService.countryExists("CZE")).thenReturn(true);
        when(countryDataService.countryExists("ITA")).thenReturn(true);


        List<String> route = routingService.calculateRoute("CZE", "ITA");


        assertNotNull(route);
        assertEquals(3, route.size()); // Shortest: CZE -> AUT -> ITA
        assertEquals("CZE", route.get(0));
        assertEquals("AUT", route.get(1));
        assertEquals("ITA", route.get(2));
    }

    @Test
    @DisplayName("Should return single country when origin equals destination")
    void testSameOriginAndDestination() {

        when(countryDataService.countryExists("USA")).thenReturn(true);

        List<String> route = routingService.calculateRoute("USA", "USA");

        assertNotNull(route);
        assertEquals(1, route.size());
        assertEquals("USA", route.get(0));

        // Verify: countryExists is called twice (once for origin, once for destination)
        verify(countryDataService, times(2)).countryExists("USA");
        verify(countryDataService, never()).getAdjacencyMap();
    }

    @Test
    @DisplayName("Should throw NoRouteFoundException when countries are not connected")
    void testNoRouteAvailable() {

        Map<String, Set<String>> adjacencyMap = new HashMap<>();
        adjacencyMap.put("USA", Set.of("CAN", "MEX"));
        adjacencyMap.put("CAN", Set.of("USA"));
        adjacencyMap.put("MEX", Set.of("USA"));
        adjacencyMap.put("AUS", Set.of()); // Australia - no land borders

        when(countryDataService.getAdjacencyMap()).thenReturn(adjacencyMap);
        when(countryDataService.countryExists("USA")).thenReturn(true);
        when(countryDataService.countryExists("AUS")).thenReturn(true);

        NoRouteFoundException exception = assertThrows(
                NoRouteFoundException.class,
                () -> routingService.calculateRoute("USA", "AUS")
        );

        assertTrue(exception.getMessage().contains("USA"));
        assertTrue(exception.getMessage().contains("AUS"));
    }

    @Test
    @DisplayName("Should throw NoRouteFoundException when origin country is invalid")
    void testInvalidOriginCountry() {

        when(countryDataService.countryExists("XXX")).thenReturn(false);

        NoRouteFoundException exception = assertThrows(
                NoRouteFoundException.class,
                () -> routingService.calculateRoute("XXX", "USA")
        );

        assertTrue(exception.getMessage().contains("XXX"));
        verify(countryDataService).countryExists("XXX");
        verify(countryDataService, never()).getAdjacencyMap();
    }

    @Test
    @DisplayName("Should throw NoRouteFoundException when destination country is invalid")
    void testInvalidDestinationCountry() {
        // Arrange
        when(countryDataService.countryExists("USA")).thenReturn(true);
        when(countryDataService.countryExists("YYY")).thenReturn(false);

        // Act & Assert
        NoRouteFoundException exception = assertThrows(
                NoRouteFoundException.class,
                () -> routingService.calculateRoute("USA", "YYY")
        );

        assertTrue(exception.getMessage().contains("YYY"));
        verify(countryDataService).countryExists("USA");
        verify(countryDataService).countryExists("YYY");
        verify(countryDataService, never()).getAdjacencyMap();
    }

    @Test
    @DisplayName("Should handle complex European routing correctly")
    void testComplexEuropeanRoute() {
        // Arrange: Create a realistic European border network
        Map<String, Set<String>> adjacencyMap = createEuropeanBorderMap();

        when(countryDataService.getAdjacencyMap()).thenReturn(adjacencyMap);
        when(countryDataService.countryExists("PRT")).thenReturn(true);
        when(countryDataService.countryExists("UKR")).thenReturn(true);

        // Act
        List<String> route = routingService.calculateRoute("PRT", "UKR");

        // Assert
        assertNotNull(route);
        assertTrue(route.size() > 2); // Multi-hop route
        assertEquals("PRT", route.get(0));
        assertEquals("UKR", route.get(route.size() - 1));

        // Verify path continuity
        for (int i = 0; i < route.size() - 1; i++) {
            String current = route.get(i);
            String next = route.get(i + 1);
            assertTrue(
                    adjacencyMap.get(current).contains(next),
                    String.format("%s should be neighbor of %s", next, current)
            );
        }
    }

    @Test
    @DisplayName("Should find optimal route when multiple paths exist")
    void testOptimalPathSelection() {
        // Arrange: Graph with multiple paths, BFS should find shortest
        Map<String, Set<String>> adjacencyMap = new HashMap<>();
        adjacencyMap.put("A", Set.of("B", "C"));
        adjacencyMap.put("B", Set.of("A", "D", "E"));
        adjacencyMap.put("C", Set.of("A", "F"));
        adjacencyMap.put("D", Set.of("B", "G"));
        adjacencyMap.put("E", Set.of("B", "F", "G"));
        adjacencyMap.put("F", Set.of("C", "E", "G"));
        adjacencyMap.put("G", Set.of("D", "E", "F"));

        when(countryDataService.getAdjacencyMap()).thenReturn(adjacencyMap);
        when(countryDataService.countryExists("A")).thenReturn(true);
        when(countryDataService.countryExists("G")).thenReturn(true);

        // Act
        List<String> route = routingService.calculateRoute("A", "G");

        // Assert
        assertNotNull(route);
        // Shortest path is A -> B -> E -> G (4 nodes)
        assertEquals(4, route.size());
        assertEquals("A", route.get(0));
        assertEquals("G", route.get(route.size() - 1));
    }

    /**
     * Helper method to create a realistic European border network.
     */
    private Map<String, Set<String>> createEuropeanBorderMap() {
        Map<String, Set<String>> map = new HashMap<>();

        map.put("PRT", Set.of("ESP"));
        map.put("ESP", Set.of("PRT", "FRA", "AND"));
        map.put("FRA", Set.of("ESP", "ITA", "CHE", "DEU", "BEL", "LUX", "AND", "MCO"));
        map.put("ITA", Set.of("FRA", "CHE", "AUT", "SVN", "SMR", "VAT"));
        map.put("CHE", Set.of("FRA", "ITA", "AUT", "DEU", "LIE"));
        map.put("AUT", Set.of("CHE", "ITA", "SVN", "HUN", "SVK", "CZE", "DEU", "LIE"));
        map.put("DEU", Set.of("FRA", "CHE", "AUT", "CZE", "POL", "DNK", "NLD", "BEL", "LUX"));
        map.put("POL", Set.of("DEU", "CZE", "SVK", "UKR", "BLR", "LTU", "RUS"));
        map.put("UKR", Set.of("POL", "SVK", "HUN", "ROU", "MDA", "BLR", "RUS"));
        map.put("SVK", Set.of("POL", "CZE", "AUT", "HUN", "UKR"));
        map.put("HUN", Set.of("AUT", "SVK", "UKR", "ROU", "SRB", "HRV", "SVN"));
        map.put("CZE", Set.of("DEU", "POL", "SVK", "AUT"));
        map.put("SVN", Set.of("ITA", "AUT", "HUN", "HRV"));

        return map;
    }
}