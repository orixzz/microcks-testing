# microcks-testing
Testing Microcks openapi mocking


https://microcks.io/documentation/references/artifacts/openapi-conventions/


/owner/{owner}/car/{car}:
  delete:
    parameters:
      - name: owner
        in: path
        description: Owner of the cars
        required: true
        schema:
          format: string
          type: string
        examples:
          laurent_307:
            value: laurent
          laurent_jp:
            value: laurent
      - name: car
        in: path
        description: Owner of the cars
        required: true
        schema:
          format: string
          type: string
        examples:
          laurent_307:
            value: '307'
          laurent_jp:
            value: 'jean-pierre'
responses:
  204:
    description: No Content
    x-microcks-refs:
      - laurent_307
      - laurent_jp
copy
When Microcks will receive DELETE /owner/laurent/car/307 or DELETE /owner/laurent/car/jean-pierre call, it will just reply using a 204 HTTP response code.
