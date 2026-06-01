package com.wanli.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wanli.llm")
public class LlmConfig {
    private String provider;
    private String apiKey;
    private String model;
    private double temperature;
    private int maxTokens;
    private String baseUrl;
}
