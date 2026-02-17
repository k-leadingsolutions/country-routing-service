package com.routing.service;

import java.util.List;

/**
 * Service interface for calculating routes between countries.
 * 
 * Follows Interface Segregation Principle (ISP) by defining only
 * the essential routing operation.
 */
public interface RoutingService {
    
    /**
     * Calculates the shortest land route between two countries.
     * 
     * @param origin the cca3 code of the starting country
     * @param destination the cca3 code of the destination country
     * @return a list of cca3 codes representing the route from origin to destination
     * @throws com.routing.exception.NoRouteFoundException if no land route exists
     */
    List<String> calculateRoute(String origin, String destination);
}