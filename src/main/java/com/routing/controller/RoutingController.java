package com.routing.controller;

import com.routing.dto.RouteResponse;
import com.routing.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for routing endpoints.
 *
 */
@Slf4j
@RestController
@RequestMapping("/routing")
@RequiredArgsConstructor
@Tag(name = "Routing", description = "Country routing API")
public class RoutingController {
    
    private final RoutingService routingService;
    
    /**
     * Calculates the shortest land route between two countries.
     * 
     * @param origin the cca3 code of the origin country
     * @param destination the cca3 code of the destination country
     * @return RouteResponse containing the calculated route
     */
    @Operation(
            summary = "Calculate shortest land route between countries",
            description = "Returns the shortest land route from origin to destination using BFS algorithm"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route found successfully"),
            @ApiResponse(responseCode = "400", description = "No land route exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> calculateRoute(
            @PathVariable String origin,
            @PathVariable String destination) {
        
        log.info("Received routing request: {} -> {}", origin, destination);
        
        List<String> route = routingService.calculateRoute(
                origin.toUpperCase(), 
                destination.toUpperCase()
        );
        
        RouteResponse response = new RouteResponse(route);
        
        return ResponseEntity.ok(response);
    }
}