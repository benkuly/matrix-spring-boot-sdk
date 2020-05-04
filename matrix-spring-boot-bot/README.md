# matrix-spring-boot-bot

This is a matrix bot framework for spring boot.

## How to use
Just add the maven/gradle dependency `net.folivo.matrix:matrix-spring-boot-bot` to you project and add the following properties to your `application.yml` or `application.properties` file:

```yaml
matrix:
  bot:
    mode: CLIENT
  client:
    homeServer:
      hostname: example.org
    token: superSecretMatrixToken
```

By default, this framework saves the sync token into a JPA-compatible database. To make it persistent you need to add some additional properties.

The following examples uses an embedded HSQLDB with the dependency `org.hsqldb:hsqldb`:
```yaml
spring:
  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:file:db/db
    username: matrixbot
    password: matrixbot
  jpa:
    hibernate:
      ddl-auto: update
```

### Appservice mode
To use the more advanced appservice mode, you must add some properties:

```yaml
matrix:
  bot:
    mode: APPSERVICE
  appservice:
    hsToken: superSecretToken
```

### Handle messages
Simple implement [MatrixMessageEventHandler](./src/main/kotlin/net/folivo/matrix/bot/handler/MatrixMessageEventHandler.kt) and make it available as bean (annotate it with `@Component`).

## Examples
Examples can be found [here](../matrix-spring-boot-bot-examples).