package com.github.observer;

import com.github.observer.controller.ObserverController;
import com.github.observer.exception.UserNotFoundException;
import com.github.observer.model.BranchDetails;
import com.github.observer.model.RepositoryDetails;
import com.github.observer.service.ObserverService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ObserverController.class)
class ObserverIntegrationTest {

    @Autowired
    private WebTestClient observerWebClient;

    @MockBean
    private ObserverService observerService;

    @Test
    @DisplayName("Should return list of repositories for valid username")
    void findRepositories_ValidUsername_ShouldReturnRepositories() {
        String username = "validUser";

        // Given
        RepositoryDetails repositoryDetails = new RepositoryDetails("repositoryName", "ownerLogin",
                List.of(new BranchDetails("branchName", "sha")));
        given(observerService.findRepositories(username)).willReturn(Flux.just(repositoryDetails));

        // When
        observerWebClient.get().uri("/repositories/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoryDetails.class)
                .hasSize(1)
                .contains(repositoryDetails);
    }

    @Test
    @DisplayName("Should return 404 for non-existing GitHub user with custom message")
    void findRepositories_NonExistingUser_ShouldReturnCustomNotFoundMessage() {
        String username = "nonExistingUser";

        // Given
        given(observerService
                .findRepositories(username))
                .willThrow(new UserNotFoundException("User not found: " + username));

        // When
        observerWebClient.get().uri("/repositories/{username}", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User not found: " + username);
    }

    // HttpMediaTypeNotSupportedException raises HttpStatus 400.
    // We handle this exception in GlobalExceptionHandler and return error message in correct format
    @Test
    @DisplayName("Should return 400 when Accept header is not application/json")
    void findRepositories_InvalidAcceptHeader_ShouldReturnNotAcceptable() {
        String username = "validUser";

        // Given
        RepositoryDetails repositoryDetails = new RepositoryDetails("repositoryName", "ownerLogin",
        List.of(new BranchDetails("branchName", "sha")));
        given(observerService.findRepositories(username)).willReturn(Flux.just(repositoryDetails));

        // When
        observerWebClient.get().uri("/repositories/{username}", username)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
