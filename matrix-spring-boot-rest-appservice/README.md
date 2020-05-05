# matrix-spring-boot-rest-appservice

This is a matrix app service for spring boot.

## How to use
Just add the maven/gradle dependency `net.folivo.matrix:matrix-spring-boot-rest-appservice` to you project and add the following properties to your `application.yml` or `application.properties` file:

```yaml
matrix:
  appservice:
    hsToken: superSecretToken
    asUsername: superAppservice
    namespaces:
      users:
        - exclusive: true
          regex: "@_superAppservice_.*"
      aliases:
        - exclusive: false
          regex: "#_superAppservice_.*"
      rooms: []
```

You also need to add properties from [matrix-spring-boot-rest-client](../matrix-spring-boot-rest-client).
See also the [spec](https://matrix.org/docs/spec/application_service/r0.1.2#registration) for more information about the properties.