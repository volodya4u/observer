package com.github.observer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ObserverConfiguration {

    @Value("${observer.baseUrl}")
    private String observerBaseUrl;
    
    @Bean
    public WebClient observerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(observerBaseUrl)
                .build();
    }
}
