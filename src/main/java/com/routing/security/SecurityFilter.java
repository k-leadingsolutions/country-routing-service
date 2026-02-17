package com.routing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Interface for security filters.
 * Stub
 *
 * Defines the contract for authentication and authorization filters.
 */
public interface SecurityFilter {
    
    /**
     * Processes the security filter logic.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException;
    
    /**
     * Checks if the request should be filtered.
     * 
     * @param request HTTP request
     * @return true if request should be filtered, false to skip
     */
    boolean shouldFilter(HttpServletRequest request);
    
    /**
     * Gets the filter order/priority.
     * Lower values have higher priority.
     * 
     * @return filter order
     */
    int getOrder();
}