package be.ori;

import io.github.microcks.testcontainers.MicrocksContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
@SpringBootTest
public class MicrocksTesting {

    @Container
    @SuppressWarnings("resource")
    static final MicrocksContainer MICROCKS = new MicrocksContainer(
            DockerImageName.parse("quay.io/microcks/microcks-uber:latest"))
            .withMainArtifacts("library-api.yaml");

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    @Test
    void shouldStartMicrocks() {
        System.out.println("Microcks URL: " + MICROCKS.getHttpEndpoint());
        System.out.println("Mock URL: " + MICROCKS.getRestMockEndpoint("Library API", "1.0.0"));
    }

    @Test
    void shouldListBooks() {
        // When
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0))
                .containsEntry("title", "The Hobbit")
                .containsEntry("author", "J.R.R. Tolkien");
    }

    @Test
    void shouldGetBookById() {
        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("title", "The Hobbit")
                .containsEntry("author", "J.R.R. Tolkien");
    }

    @Test
    void shouldReturn404ForNonExistentBook() {
        try {
            // When
            restTemplate.exchange(
                    MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/999",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            fail("Should have thrown an exception");
        } catch (HttpClientErrorException e) {
            // Then
            assertThat(e.getStatusCode().value()).isEqualTo(404);
            assertThat(e.getResponseBodyAs(Map.class))
                    .containsEntry("message", "Book not found");
        }
    }
}
