package com.routing.service.impl;

import com.routing.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CachedRoutingService implements RoutingService {
    private final RoutingService delegate;
    private final Map<String, List<String>> routeCache = new ConcurrentHashMap<>();
    
    @Override
    public List<String> calculateRoute(String origin, String destination) {
        String key = origin + "->" + destination;
        return routeCache.computeIfAbsent(key, 
            k -> delegate.calculateRoute(origin, destination));
    }
}