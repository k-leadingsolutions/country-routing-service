package com.routing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 * 
 * Provides comprehensive API documentation with interactive UI.
 */
@Configuration
public class SwaggerConfig {
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    /**
     * Configures OpenAPI documentation.
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Country Routing Service API")
                        .version("1.0.0")
                        .description("""
                                RESTful API for calculating the shortest land route between countries.
                                
                                **Features:**
                                - BFS algorithm ensures shortest path
                                - Supports 250+ countries
                                - Cached data for fast response times
                                - Comprehensive error handling
                                
                                **Algorithm:** Breadth-First Search (BFS) guarantees the route with fewest border crossings.
                                
                                **Data Source:** [mledoze/countries](https://github.com/mledoze/countries)
                                """)
                        .contact(new Contact()
                                .name("Country Routing Service Team")
                                .email("support@routing.com")
                                .url("https://github.com/your-org/country-routing-service"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.routing.com")
                                .description("Production Server")
                ));
    }
}