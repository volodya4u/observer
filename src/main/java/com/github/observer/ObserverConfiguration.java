package com.github.observer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ObserverConfiguration {

    @Value("${observer.baseUrl}")
    private String observerBaseUrl;

    @Value("${GITHUB_TOKEN}")
    private String githubToken;
    
    @Bean
    public WebClient observerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(observerBaseUrl)
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .build();
    }
}
