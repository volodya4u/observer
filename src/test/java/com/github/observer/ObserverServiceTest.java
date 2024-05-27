package com.github.observer;

import com.github.observer.exception.UserNotFoundException;
import com.github.observer.model.*;
import com.github.observer.service.ObserverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

        Mono<List<RepositoryDetails>> result = observerService.findRepositories(userName, false);

        StepVerifier.create(result)
                .expectNextMatches(repos -> repos.size() == 2 &&
                        repos.stream().anyMatch(repo -> repo.getName().equals(repo1) && repo.getOwner().equals(userName)) &&
                        repos.stream().anyMatch(repo -> repo.getName().equals(repo2) && repo.getOwner().equals(userName)))
                .verifyComplete();

        verify(observerWebClient, times(3)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/users/{username}/repos", userName);
        verify(requestHeadersSpec, times(3)).retrieve();
        verify(responseSpec, times(1)).bodyToFlux(Repository.class);
    }

    @Test
    public void testFindRepositories_EmptyResponse() {
        String userName = "userWithNoRepositories";

        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.empty());

        Mono<List<RepositoryDetails>> result = observerService.findRepositories(userName, false);

        StepVerifier.create(result)
                .expectNextMatches(List::isEmpty)
                .verifyComplete();

        verify(observerWebClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/users/{username}/repos", userName);
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToFlux(Repository.class);
    }

    @Test
    public void testFindRepositories_UserNotFound_ReturnsError() {
        String userName = "unknownUser";
        String errorMessage = "User not found: " + userName;

        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenThrow(new UserNotFoundException(errorMessage));

        assertThatThrownBy(() -> observerService.findRepositories(userName, false).block())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(errorMessage);

        verify(observerWebClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/users/{username}/repos", userName);
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).onStatus(any(), any());
    }

    @Test
    public void testFindRepositories_WithForks_ReturnsOnlyForks() {
        String userName = "user1";
        String repo1 = "repo1";
        String repo2 = "repo2";

        Repository repository1 = new Repository(repo1, new Owner(userName), true);
        Repository repository2 = new Repository(repo2, new Owner(userName), false);
        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", userName))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersUriSpec.uri("/repos/" + userName + "/"+ repo1 + "/branches"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Repository.class)).thenReturn(Flux.just(repository1, repository2));
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.empty());

        Mono<List<RepositoryDetails>> result = observerService.findRepositories(userName, true);

        StepVerifier.create(result)
                .expectNextMatches(repos -> repos.size() == 1 &&
                        repos.getFirst().getName().equals(repo1) && repos.getFirst().getOwner().equals(userName))
                .verifyComplete();

        verify(observerWebClient, times(2)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/users/{username}/repos", userName);
        verify(requestHeadersSpec, times(2)).retrieve();
        verify(responseSpec, times(1)).bodyToFlux(Repository.class);
    }

    @Test
    public void testGetBranches_ReturnsBranchDetails() {
        String userName = "user1";
        String repo1 = "repo1";
        String repositoryFullName = userName + "/"+ repo1;
        Branch branch = new Branch("main", new Commit("sha123"));

        when(observerWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/repos/" + userName + "/"+ repo1 + "/branches"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Branch.class)).thenReturn(Flux.just(branch));

        Flux<BranchDetails> result = observerService.getBranches(repositoryFullName);

        StepVerifier.create(result)
                .expectNextMatches(branchDetails ->
                        branchDetails.getName().equals(branch.getName()) &&
                                branchDetails.getLastCommitSha().equals(branch.getCommit().getSha()))
                .verifyComplete();

        verify(observerWebClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/repos/" + userName + "/"+ repo1 + "/branches");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).bodyToFlux(Branch.class);
    }

    @Test
    public void testFallbackFindRepositories_ReturnsError() {
        String userName = "user1";
        Throwable t = new RuntimeException("API call failed");

        assertThatThrownBy(() -> observerService.fallbackFindRepositories(userName, true, t).block())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userName);

        verifyNoInteractions(observerWebClient);
    }
}
