# matrix-spring-boot-rest-client

This is a matrix client for spring boot.

## How to use
Just add the maven/gradle dependency `net.folivo.matrix:matrix-spring-boot-rest-client` to you project and add the following properties to your `application.yml` or `application.properties` file:

```yaml
matrix:
  homeServer:
    hostname: example.org
  token: superSecretMatrixToken
```

Now a Bean of type `MatrixClient` is created, which can be autowired.

## Persistent sync
By default, the sync batch token from the matrix server is only saved in memory. You can persist it by implementing the `SyncBatchTokenService`. Your implementation must be available as bean, so either annotate it with `@Service` or define it as `@Bean` in an `@Configuration` class.
