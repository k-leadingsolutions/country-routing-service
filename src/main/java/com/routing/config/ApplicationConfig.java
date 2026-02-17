package com.routing.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Application configuration class.
 *
 * Defines beans required by the application, including WebClient
 * with optimized HTTP client configuration.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Creates a WebClient.Builder bean with custom HTTP client configuration.
     *
     * Configures:
     * - Connection timeout: 15 seconds
     * - Read timeout: 30 seconds
     * - Write timeout: 30 seconds
     * - Response timeout: 30 seconds
     *
     * @return configured WebClient.Builder instance
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure Netty HttpClient with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15_000) // 15 seconds
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        // Build WebClient with the configured HttpClient
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}