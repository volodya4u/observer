package com.github.observer;

import com.github.observer.exception.UserNotFoundException;
import com.github.observer.model.Branch;
import com.github.observer.model.Owner;
import com.github.observer.model.Repository;
import com.github.observer.model.RepositoryDetails;
import com.github.observer.service.ObserverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObserverServiceTest {

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    @Mock
    WebClient observerWebClient;

    @InjectMocks
    ObserverService observerService;

    @Test
    public void testFindRepositories_UserFound_ReturnsRepositories() {
        String userName = "user1";
        String repo1 = "repo1";
        String repo2 = "repo2";

        // Mock data
        Repository repository1 = new Repository(repo1, new Owner(userName), false);
        Repository repository2 = new Repository(repo2, new Owner(userName), false);
        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersUriSpec.uri("/repos/" + userName + "/"+ repo1 + "/branches"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersUriSpec.uri("/repos/" + userName + "/"+ repo2 + "/branches"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.just(repository1, repository2));
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.empty());

        // Test
        Flux<RepositoryDetails> result = observerService.findRepositories(userName);

        // Verify
        StepVerifier.create(result)
                .expectNextMatches(repo -> repo.getName().equals(repo1) && repo.getOwner().equals(userName))
                .expectNextMatches(repo -> repo.getName().equals(repo2) && repo.getOwner().equals(userName))
                .verifyComplete();
    }

    @Test
    public void testFindRepositories_EmptyResponse() {
        String userName = "userWithNoRepositories";

        // Mock empty response from WebClient
        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.empty());

        // Test
        Flux<RepositoryDetails> result = observerService.findRepositories(userName);

        // Verify
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void testFindRepositories_UserNotFound_ReturnsError() {
        String userName = "unknownUser";
        String errorMessage = "User not found: " + userName;

        // Mock WebClient behavior
        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenThrow(new UserNotFoundException(errorMessage));

        // Perform the test
        assertThatThrownBy(() -> observerService.findRepositories(userName))
                .isInstanceOf(UserNotFoundException.class);
    }
}
