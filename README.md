# Microcks Testing Demo
This project showcases how to leverage Microcks, TestContainers, and Spring Boot for automated API testing.

## Prerequisites
- Java 22
- Maven
- A container runtime:
  - Docker Desktop, OR
  - Podman, OR
  - Colima, OR
  - Any other OCI-compatible container runtime
  
> Note: TestContainers will work with any OCI-compatible container runtime, not just Docker Desktop.

## Technical Stack
### Microcks Implementation
- **Version**: microcks-testcontainers v0.3.0
- **Scripting Capabilities**:
  - Dynamic response templating using Freemarker
  - Support for custom delay patterns
  - Request/response pattern matching

### Reference Implementation Examples
1. **Example-based Mocking**
    ```yaml
    # GET /books response example
    responses:
      '200':
        description: List of books
        content:
          application/json:
            examples:
              list_all_books:
                value: [
                  {
                    "id": "1",
                    "title": "The Hobbit",
                    "author": "J.R.R. Tolkien",
                    "isbn": "978-0547928227",
                    "publishedYear": 1937
                  },
                  {
                    "id": "2",
                    "title": "1984",
                    "author": "George Orwell",
                    "isbn": "978-0451524935",
                    "publishedYear": 1949
                  }
                ]
    ```

2. **Rule-based Responses with Script Dispatcher**
    ```yaml
    # GET /books/{id} dispatcher example
    x-microcks-operation:
      delay: 3000
      dispatcher: SCRIPT
      dispatcherRules: |
        def url = mockRequest.getRequest().getRequestURI();
        if (url.endsWith("/books/999")) {
            return "get_book_not_found";
        }
        return "get_book_1";
    ```

3. **Dynamic Response Generation with Templates**
    ```yaml
    # GET /books/latest response example
    value: {
      "id": "{{uuid()}}",
      "title": "1984",
      "author": "{{randomFullName()}}",
      "isbn": "{{randomInt(100000000000, 999999999999)}}",
      "publishedYear": 1949,
      "serverTime": "{{now('yyyy-MM-dd HH:mm:ss')}}",
      "dynamicValues": {
        "randomEmail": "{{randomEmail()}}",
        "randomCity": "{{randomCity()}}",
        "randomPhoneNumber": "{{randomPhoneNumber()}}",
        "randomBoolean": "{{randomBoolean()}}",
        "randomString": "{{randomString(10)}}"
      }
    }
    ```

4. **Multiple Response References**
    ```yaml
    # DELETE /books/{id} response example
    '204':
      description: Book successfully deleted
      x-microcks-refs:
        - delete_hobbit
        - delete_1984
    ```

### Operations Support
- REST API mocking (GET, POST, PUT, DELETE)
- Response status code simulation
- Header manipulation
- Content-type handling

## Additional Features Available
These features are not demonstrated in this project but are available in Microcks:

1. **Advanced Dispatching**
    ```yaml
    x-microcks-operation:
      dispatcher: URI_ELEMENTS
      dispatcherRules: id in [1, 2, 3] ? success : not_found
    ```

2. **Content Negotiation**
    ```yaml
    responses:
      '200':
        content:
          application/json:
            example: { "name": "book" }
          application/xml:
            example: "<book><name>book</name></book>"
    ```

3. **Request Validation**
    ```yaml
    x-microcks-operation:
      validator: STRICT
      validatorRules: |
        headers['Authorization'] != null
        && body.price > 0
    ```

4. **Complex Delay Patterns**
    ```yaml
    x-microcks-operation:
      delay: RANDOM
      delayRules: |
        between 100 and 500
        when headers['priority'] == 'high' then 50
        when path contains 'slow' then 2000
    ```

5. **Event-Driven APIs**
    ```yaml
    asyncapi: '2.0.0'
    channels:
      book.events:
        publish:
          message:
            payload:
              type: object
    ```

6. **OAuth2/OpenID Support**
    ```yaml
    security:
      - oauth2:
          - read
          - write
    x-microcks-security:
      keycloak:
        realm: library-realm
        resource: library-api
    ```

## Future Enhancements
- Dynamic URL injection for external service testing using Spring's DynamicPropertySource

## Resources
- [Microcks documentation](https://microcks.io/documentation)
- [Microcks TestContainers Java GitHub repo](https://github.com/microcks/microcks-testcontainers-java)
- [OpenAPI Conventions Documentations](https://microcks.io/documentation/references/artifacts/openapi-conventions/)
- [Spring Dynamic Property Source](https://www.baeldung.com/spring-dynamicpropertysource)
