package com.github.observer.service;

import com.github.observer.exception.UserNotFoundException;
import com.github.observer.model.Branch;
import com.github.observer.model.BranchDetails;
import com.github.observer.model.Repository;
import com.github.observer.model.RepositoryDetails;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class ObserverService {

    private final WebClient observerWebClient;

    public ObserverService(WebClient observerWebClient) {
        this.observerWebClient = observerWebClient;
    }

    @CircuitBreaker(name = "CircuitBreakerService", fallbackMethod = "fallbackMethod")
    public Mono<List<RepositoryDetails>> findRepositories(@NotBlank String username, boolean fork) {

        log.debug("Getting repositories for user: {}", username);
        return observerWebClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new UserNotFoundException("User not found: " + username)))
                .bodyToFlux(Repository.class)
                .filter(repository -> repository.isFork() == fork)
                .flatMap(this::convertToRepositoryDetails)
                .doOnComplete(() -> log.info("Finished getting repositories for user: {}", username))
                .collectList();
    }

    private Flux<RepositoryDetails> fallbackMethod(String username, boolean fork, Throwable t) {
        log.error("Error getting repositories for user: {}, error: {}", username, t.getMessage());
        throw new UserNotFoundException("User not found: " + username);
    }

    private Flux<RepositoryDetails> convertToRepositoryDetails(Repository repository) {
        return getBranches(repository.getOwner().getLogin() + "/" + repository.getName())
                .collectList()
                .map(branches -> new RepositoryDetails(repository.getName(),
                        repository.getOwner().getLogin(), branches))
                .flux();
    }

    private Flux<BranchDetails> getBranches(String repositoryFullName) {
        return observerWebClient.get()
                .uri("/repos/" + repositoryFullName + "/branches")
                .retrieve()
                .bodyToFlux(Branch.class)
                .map(branch -> new BranchDetails(branch.getName(), branch.getCommit().getSha()));
    }
}
