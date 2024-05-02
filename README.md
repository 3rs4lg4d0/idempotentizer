[![CI pipeline](https://github.com/3rs4lg4d0/idempotentizer/actions/workflows/ci.yaml/badge.svg)](https://github.com/3rs4lg4d0/idempotentizer/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/github/3rs4lg4d0/idempotentizer/graph/badge.svg?token=RJycX1Nh4H)](https://codecov.io/github/3rs4lg4d0/idempotentizer)
[![LICENSE](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

# Idempotentizer

The Idempotentizer is a Java library designed to simplify the process of implementing idempotent behavior in your consumer applications. Idempotence is a crucial concept in distributed systems and APIs, ensuring that repeated requests have the same effect as the initial request, even if the request is executed multiple times.

## Getting Started

To get started with Idempotentizer in your Java project, follow these simple steps:

1. **Add Idempotentizer to Your Project**: Include Idempotentizer as a dependency in your project. You can either download the JAR file manually or use a dependency management tool like Maven or Gradle.

    ```xml
    <!-- Maven -->
    <dependency>
        <groupId>com.ersalgado</groupId>
        <artifactId>idempotentizer</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

    ```groovy
    // Gradle
    implementation 'com.ersalgado:idempotentizer:1.0.0'
    ```

2. **Create an instance**: Create an instance of *Idempotentizer*, providing one of the available repository implementations.

    ```java
    // Create an instance of one of the provided repositories.
    Repository repository = new SQLRepository(datasource, new JacksonObjectSerde());

    // Instances of Idempotentizer are thread safe so you can
    // use this instance as a singleton in your application.
    Idempotentizer idempotentizer = new DefaultIdempotentizer(repository);
    ```

3. **Handle Idempotent Requests**: Use Idempotentizer to check if incoming requests were already processed, ensuring that duplicate requests are detected and processed accordingly.

    ```java
    private void doUpdateInventoryOnce(UpdateInventoryRequest request, UUID idempotencyKey) {
        var requestInfo = idempotentizer.checkProcessed(idempotencyKey, CONSUMER_ID);
        if (!requestInfo.getProcessed()) {

            // Process the request...

            // Mark the request as processed.
            idempotentizer.markAsProcessed(idempotencyKey, CONSUMER_ID);
        } else {
            log.debug("The request '{}' for consumer '{}' was already processed", idempotencyKey, CONSUMER_ID);
        }
    }
    ```

    You can store any result object generated after successfully processing a request to ensure consistency for all subsequent duplicate requests. This stored result can then be returned when duplicate calls are detected, maintaining the integrity of the system.

    ```java
    private MyResult doUpdateInventoryOnce(UpdateInventoryRequest request, UUID idempotencyKey) {
        var requestInfo = idempotentizer.checkProcessed(idempotencyKey, CONSUMER_ID);
        if (!requestInfo.getProcessed()) {
            var result = // Process the request and get a result...

            // Mark the request as processed, providing a result.
            idempotentizer.markAsProcessed(idempotencyKey, CONSUMER_ID, result);
            
            return result;
        } else {
            log.debug("The request '{}' for consumer '{}' was already processed", idempotencyKey, CONSUMER_ID);
            return (MyResult) requestInfo.getReturnedValue();
        }
    }

## Contributing

Contributions to Idempotentizer are welcome! If you encounter any issues, have ideas for improvements, or would like to contribute code, please submit a pull request or open an issue.

## License

The Idempotentizer library is open-source software licensed under the [MIT License](https://opensource.org/licenses/MIT).

