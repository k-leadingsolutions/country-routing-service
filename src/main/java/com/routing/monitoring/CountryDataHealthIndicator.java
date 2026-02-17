package com.routing.monitoring;

import com.routing.service.impl.CountryDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Custom health indicator for country data service.
 *
 * Provides detailed health information about the cached country data.
 */
@Component
@RequiredArgsConstructor
public class CountryDataHealthIndicator implements HealthIndicator {

    private final CountryDataService countryDataService;
    private final LocalDateTime startupTime = LocalDateTime.now();

    @Override
    public Health health() {
        try {
            int countryCount = countryDataService.getAdjacencyMap().size();

            if (countryCount == 0) {
                return Health.down()
                        .withDetail("countries", countryCount)
                        .withDetail("error", "No country data loaded")
                        .build();
            }

            long uptimeMinutes = Duration.between(startupTime, LocalDateTime.now()).toMinutes();

            return Health.up()
                    .withDetail("countries", countryCount)
                    .withDetail("dataLoadedAt", startupTime)
                    .withDetail("uptimeMinutes", uptimeMinutes)
                    .withDetail("status", "Country data successfully loaded and cached")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}