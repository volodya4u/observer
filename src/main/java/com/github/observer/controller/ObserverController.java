package com.github.observer.controller;

import com.github.observer.model.RepositoryDetails;
import com.github.observer.service.ObserverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/repositories")
public class ObserverController {

    private final ObserverService observerService;

    public ObserverController(ObserverService observerService) {
        this.observerService = observerService;
    }

    @GetMapping(value = "/{username}/{fork}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<RepositoryDetails>>> getGithubRepositories(@PathVariable String username,
                                                                               @PathVariable boolean fork) {
        log.debug("ObserverController execution with username: {}", username);
        return observerService.findRepositories(username, fork)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
