package com.routing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for security filters.
 * Stub
 *
 * Provides common functionality for all security filters including:
 * - Public endpoint exclusion
 * - Error response formatting
 * - Logging
 */
@Slf4j
public abstract class AbstractSecurityFilter extends OncePerRequestFilter implements SecurityFilter {
    
    /**
     * Default public endpoints that don't require authentication.
     */
    protected static final List<String> DEFAULT_PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/auth/",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health",
            "/actuator/info"
    );
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Skip filter for public endpoints
        if (isPublicEndpoint(request)) {
            log.debug("Skipping security filter for public endpoint: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check if this filter should process the request
        if (!shouldFilter(request)) {
            log.debug("Filter not applicable for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("Processing security filter for: {} {}", request.getMethod(), request.getRequestURI());
        
        try {
            // Delegate to implementation
            doFilter(request, response, filterChain);
        } catch (Exception e) {
            log.error("Security filter error: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Security processing error");
        }
    }
    
    /**
     * Checks if the endpoint is public (doesn't require authentication).
     * 
     * @param request HTTP request
     * @return true if public endpoint
     */
    protected boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return DEFAULT_PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith) || getAdditionalPublicEndpoints().stream()
                .anyMatch(path::startsWith);
    }
    
    /**
     * Gets additional public endpoints specific to the implementation.
     * Override to add custom public endpoints.
     * 
     * @return list of additional public endpoint patterns
     */
    protected List<String> getAdditionalPublicEndpoints() {
        return List.of();
    }
    
    /**
     * Sends an error response with JSON format.
     * 
     * @param response HTTP response
     * @param status HTTP status code
     * @param message error message
     * @throws IOException if I/O error occurs
     */
    protected void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonError = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now().toString(),
                status,
                getStatusText(status),
                message,
                "N/A"
        );
        
        response.getWriter().write(jsonError);
        response.getWriter().flush();
    }
    
    /**
     * Gets HTTP status text.
     * 
     * @param status HTTP status code
     * @return status text
     */
    private String getStatusText(int status) {
        return switch (status) {
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 400 -> "Bad Request";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }
    
    /**
     * Extracts header value from request.
     * 
     * @param request HTTP request
     * @param headerName header name
     * @return header value or null
     */
    protected String extractHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }
    
    /**
     * Extracts bearer token from Authorization header.
     * 
     * @param request HTTP request
     * @return bearer token or null
     */
    protected String extractBearerToken(HttpServletRequest request) {
        String authHeader = extractHeader(request, "Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * Extracts API key from header or query parameter.
     * 
     * @param request HTTP request
     * @param headerName header name
     * @param paramName query parameter name
     * @return API key or null
     */
    protected String extractApiKey(HttpServletRequest request, String headerName, String paramName) {
        // Try header first
        String apiKey = extractHeader(request, headerName);
        if (apiKey != null) {
            return apiKey;
        }
        
        // Try query parameter
        return request.getParameter(paramName);
    }
    
    @Override
    public int getOrder() {
        return 0; // Default order, override as needed
    }
}