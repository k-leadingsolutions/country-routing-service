package com.routing.service.impl;

import com.routing.exception.NoRouteFoundException;
import com.routing.monitoring.RoutingMetrics;
import com.routing.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of RoutingService using Breadth-First Search (BFS) algorithm.
 *
 * Includes metrics tracking for observability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class RoutingServiceImpl implements RoutingService {

    private final CountryDataService countryDataService;
    private final RoutingMetrics routingMetrics;

    @Override
    public List<String> calculateRoute(String origin, String destination) {
        log.info("Calculating route from {} to {}", origin, destination);

        return routingMetrics.getTimer().record(() -> {
            try {
                // Validate inputs
                validateCountries(origin, destination);

                // Edge case: origin equals destination
                if (origin.equals(destination)) {
                    log.info("Origin and destination are the same: {}", origin);
                    routingMetrics.recordCalculation();
                    return List.of(origin);
                }

                List<String> route = bfs(origin, destination);

                if (route.isEmpty()) {
                    log.warn("No route found from {} to {}", origin, destination);
                    routingMetrics.recordError();
                    throw new NoRouteFoundException(origin, destination);
                }

                log.info("Route found: {} (length: {} borders)", route, route.size() - 1);
                routingMetrics.recordCalculation();
                return route;
            } catch (NoRouteFoundException e) {
                routingMetrics.recordError();
                throw e;
            }
        });
    }

    private void validateCountries(String origin, String destination) {
        if (!countryDataService.countryExists(origin)) {
            log.error("Invalid origin country: {}", origin);
            throw new NoRouteFoundException(origin, destination);
        }

        if (!countryDataService.countryExists(destination)) {
            log.error("Invalid destination country: {}", destination);
            throw new NoRouteFoundException(origin, destination);
        }
    }

    private List<String> bfs(String origin, String destination) {
        Map<String, Set<String>> adjacencyMap = countryDataService.getAdjacencyMap();

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();

        queue.offer(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(destination)) {
                return reconstructPath(parentMap, destination);
            }

            Set<String> neighbors = adjacencyMap.getOrDefault(current, Collections.emptySet());

            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> reconstructPath(Map<String, String> parentMap, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}