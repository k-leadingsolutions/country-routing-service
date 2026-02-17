package com.routing.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for routing service.
 *
 * Tracks route calculations, errors, and performance.
 */
@Component
@Getter
public class RoutingMetrics {

    private final Counter routeCalculationCounter;
    private final Counter routeErrorCounter;
    private final Timer routeCalculationTimer;

    public RoutingMetrics(MeterRegistry registry) {
        this.routeCalculationCounter = Counter.builder("routing.calculations.total")
                .description("Total number of route calculations performed")
                .register(registry);

        this.routeErrorCounter = Counter.builder("routing.errors.total")
                .description("Total number of routing errors")
                .register(registry);

        this.routeCalculationTimer = Timer.builder("routing.calculation.duration")
                .description("Time taken to calculate routes")
                .register(registry);
    }

    /**
     * Records a successful route calculation.
     */
    public void recordCalculation() {
        routeCalculationCounter.increment();
    }

    /**
     * Records a routing error.
     */
    public void recordError() {
        routeErrorCounter.increment();
    }

    /**
     * Returns the timer for measuring calculation duration.
     */
    public Timer getTimer() {
        return routeCalculationTimer;
    }
}