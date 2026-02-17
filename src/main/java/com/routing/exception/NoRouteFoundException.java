package com.routing.exception;

/**
 * Custom exception thrown when no land route exists between two countries.
 * 
 * This exception is mapped to HTTP 400 Bad Request by the GlobalExceptionHandler.
 */
public class NoRouteFoundException extends RuntimeException {
    
    public NoRouteFoundException(String origin, String destination) {
        super(String.format("No land route found between %s and %s", origin, destination));
    }
}