# matrix-spring-boot-rest-appservice

This is a matrix app service for spring boot.

## How to use
Just add the maven/gradle dependency `net.folivo.matrix:matrix-spring-boot-rest-appservice` to you project and add the following properties to your `application.yml` or `application.properties` file:

```yaml
matrix:
  appservice:
    hsToken: superSecretToken
```

You also need to add properties from [matrix-spring-boot-rest-client](../matrix-spring-boot-rest-client).
