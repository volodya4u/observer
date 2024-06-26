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

        when(observerService.findRepositories(username, true)).thenReturn(Mono.just(List.of(
                new RepositoryDetails("repo1", "owner1", List.of()),
                new RepositoryDetails("repo2", "owner2", List.of())
        )));

        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username, true);

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

        when(observerService.findRepositories(username, true)).thenReturn(Mono.just(List.of()));

        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username, true);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity ->
                        ObjectUtils.isEmpty(responseEntity.getBody()))
                .verifyComplete();
    }

    @Test
    void testGetGithubRepositories_EmptyResponse_ReturnsNotFound() {
        String username = "emptyUser";

        when(observerService.findRepositories(username, true)).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username, true);

        StepVerifier.create(result)
                .expectNextMatches(responseEntity ->
                        ObjectUtils.isEmpty(responseEntity.getBody()))
                .verifyComplete();
    }

    @Test
    void testGetGithubRepositories_InternalServerError_ReturnsInternalServerError() {
        String username = "errorUser";

        when(observerService.findRepositories(username, true)).thenReturn(Mono.error(new RuntimeException()));

        Mono<ResponseEntity<List<RepositoryDetails>>> result
                = observerController.getGithubRepositories(username, true);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();
    }
}
