package com.github.observer;

import com.github.observer.controller.ObserverController;
import com.github.observer.model.RepositoryDetails;
import com.github.observer.service.ObserverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObserverControllerTest {

    @InjectMocks
    private ObserverController observerController;

    @Mock
    private ObserverService observerService;

    @Test
    void testGetGithubRepositories_UserFound_ReturnsRepositories() {
        String username = "testUser";

        // Mock ObserverService behavior
        when(observerService.findRepositories(username)).thenReturn(Flux.just(
                new RepositoryDetails("repo1", "owner1", List.of()),
                new RepositoryDetails("repo2", "owner2", List.of())
        ));

        // Perform the test
        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username);

        // Verify the result
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    List<RepositoryDetails> repositories = responseEntity.getBody();
                    return repositories != null && repositories.size() == 2
                            && repositories.get(0).getName().equals("repo1")
                            && repositories.get(1).getName().equals("repo2");
                })
                .verifyComplete();
    }

    @Test
    void testGetGithubRepositories_UserNotFound_ReturnsNotFound() {
        String username = "unknownUser";

        // Mock ObserverService behavior for user not found
        when(observerService.findRepositories(username)).thenReturn(Flux.empty());

        // Perform the test
        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username);

        // Verify the result
        StepVerifier.create(result)
                .expectNextMatches(responseEntity ->
                        ObjectUtils.isEmpty(responseEntity.getBody()))
                .verifyComplete();
    }
}
