package com.routing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object representing a country from the external API.
 * 
 * Uses Jackson annotations to deserialize only the required fields.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Country {
    
    /**
     * The three-letter country code (cca3) - used as the unique identifier.
     */
    private String cca3;
    
    /**
     * List of neighboring country codes (cca3) that share a land border.
     */
    private List<String> borders;
}