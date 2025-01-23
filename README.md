# microcks-testing
Testing Microcks OpenAPI mocking with TestContainers and Spring Boot

## 16-Minute Demo: Automated API Testing with Microcks

This demo showcases how to leverage Microcks, TestContainers, and Spring Boot for automated API testing.

### 1. Project Setup (3 minutes)
- Overview of the project structure
- Key components: Spring Boot test class, OpenAPI spec, and TestContainers setup
- Highlight the `MicrocksContainer` configuration and OpenAPI artifact loading

### 2. OpenAPI Specification Deep Dive (4 minutes)
- Examine the Library API specification
- Showcase mock response configuration
- Demonstrate dynamic response templating
- Explain x-microcks-operation conventions for delays and response patterns

### 3. Test Suite Walkthrough (6 minutes)
Explore comprehensive test cases:
- Basic API health check and container startup verification
- CRUD operations with mock data
- Error handling (404 responses)
- Dynamic response templating with random data
- Configured response delays
- Multiple response references using x-microcks-refs

### 4. Running and Results (3 minutes)
- Execute the test suite
- Review test outputs and logging
- Demonstrate how Microcks validates the contract between API spec and implementation

## Getting Started

https://microcks.io/documentation/references/artifacts/openapi-conventions/


## TODO
Work out dynamic URL injects to use this as an external service with application code
Dynamic Injection during testing
