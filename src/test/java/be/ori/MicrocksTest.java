package be.ori;

import io.github.microcks.testcontainers.MicrocksContainer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class MicrocksTest {
    private static final Logger log = LoggerFactory.getLogger(MicrocksTest.class);

    @Container
    @SuppressWarnings("resource")
    static final MicrocksContainer MICROCKS = new MicrocksContainer(
            DockerImageName.parse("quay.io/microcks/microcks-uber:latest"))
            .withMainArtifacts("library-api.yaml");

    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    @Test
    void shouldStartMicrocks() {
        log.info("Testing Microcks container startup");
        log.info("Microcks URL: {}", MICROCKS.getHttpEndpoint());
        log.info("Mock URL: {}", MICROCKS.getRestMockEndpoint("Library API", "1.0.0"));
    }

    @Test
    void shouldListBooks() {
        log.info("Testing GET /books endpoint with limit=10");
        // When
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        log.info("Received {} books in response", response.getBody().size());
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0))
                .containsEntry("title", "The Hobbit")
                .containsEntry("author", "J.R.R. Tolkien");
    }

    @Test
    void shouldGetBookById() {
        log.info("Testing GET /books/1 endpoint");
        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        log.info("Retrieved book: {}", response.getBody());
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody())
                .containsEntry("title", "The Hobbit")
                .containsEntry("author", "J.R.R. Tolkien");
    }

    @Test
    void shouldReturn404ForNonExistentBook() {
        log.info("Testing GET /books/999 endpoint (expecting 404)");
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
            log.info("Received expected 404 response: {}", e.getResponseBodyAsString());
            assertThat(e.getStatusCode().value()).isEqualTo(404);
            @SuppressWarnings("unchecked")
            Map<String, String> errorResponse = e.getResponseBodyAs(Map.class);
            assertThat(errorResponse).containsEntry("message", "Book not found");
        }
    }

    @Test
    void shouldDeleteBooks() {
        log.info("Testing DELETE /books endpoint for multiple books");
        // Test both reference cases from x-microcks-refs
        String[] bookIds = {"1", "2"};  // The Hobbit and 1984
        for (String bookId : bookIds) {
            log.info("Deleting book with ID: {}", bookId);
            // When
            ResponseEntity<Void> response = restTemplate.exchange(
                    MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/" + bookId,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // Then
            log.info("Successfully deleted book {}", bookId);
            assertThat(response.getStatusCode().value()).isEqualTo(204);
        }
    }

    @Test
    void shouldRespectOpenApiConfiguredDelay() {
        log.info("Testing configured delay (3000ms) from OpenAPI spec on GET /books/1 endpoint");
        long startTime = System.currentTimeMillis();

        // When
        restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - verify the configured 3000ms delay is respected
        log.info("Request took {}ms (expected >= 3000ms from x-microcks-operation.delay)", duration);
        assertThat(duration).isGreaterThanOrEqualTo(3000);
    }

    @Test
    void shouldSupportResponseTemplating() {
        log.info("Testing GET /books/latest endpoint with dynamic values");
        
        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                MICROCKS.getRestMockEndpoint("Library API", "1.0.0") + "/books/latest",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        log.info("Received response with dynamic values: {}", body);
        
        // Verify presence of dynamic values
        assertThat(body)
                .containsKey("id")
                .containsKey("author")
                .containsKey("serverTime")
                .containsKey("dynamicValues");

        @SuppressWarnings("unchecked")
        Map<String, Object> dynamicValues = (Map<String, Object>) body.get("dynamicValues");
        assertThat(dynamicValues)
                .containsKey("randomEmail")
                .containsKey("randomCity")
                .containsKey("randomPhoneNumber")
                .containsKey("randomBoolean")
                .containsKey("randomString");
    }
}
