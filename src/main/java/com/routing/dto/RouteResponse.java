package com.routing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing the calculated route information.
 * 
 * Returns the complete path from origin to destination.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    
    /**
     * The complete route as a list of country codes (cca3).
     * The first element is the origin, and the last is the destination.
     */
    private List<String> route;
}