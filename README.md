![Publish package to the Maven Central Repository](https://github.com/benkuly/matrix-spring-boot-sdk/workflows/Publish%20package%20to%20the%20Maven%20Central%20Repository/badge.svg)
![Version](https://maven-badges.herokuapp.com/maven-central/net.folivo/matrix-spring-boot-bot/badge.svg)

# matrix-spring-boot-sdk
This project contains tools to use [matrix](https://matrix.org/) with Spring Boot and is written in Kotlin. It should also work with Java, but it hasn't been tested yet. It uses [Spring Reactive](https://spring.io/reactive) and [Reactor](https://projectreactor.io/) to create unblocking applications.

* [matrix-spring-boot-core](./matrix-spring-boot-core) contains the matrix event model and some shared code.
* [matrix-spring-boot-rest-client](./matrix-spring-boot-rest-client) to interact with the matrix-api on a low level.
* [matrix-spring-boot-rest-appservice](./matrix-spring-boot-rest-appservice) to create an appservice.
* [matrix-spring-boot-bot](./matrix-spring-boot-bot) to create simple bots.
* [matrix-spring-boot-bot-examples](./matrix-spring-boot-bot-examples) contains examples how to create bots.

The most developers only will need to use [matrix-spring-boot-bot](./matrix-spring-boot-bot), which contains the other projects. Therefore, this documentation focuses on this sub-project.

## How to use
Just add the maven/gradle dependency `net.folivo.matrix:matrix-spring-boot-bot` to you project.
 
### Properties
 Add the following properties to your `application.yml` or `application.properties` file:

```yaml
matrix:
  bot:
    # The domain-part of matrix-ids. E. g. example.org when your userIds look like @unicorn:example.org
    serverName: example.org
    # (optional in mode CLIENT) The localpart (username) of the user associated with the application service
    # or just the username of your bot.
    username: superAppservice
    # (optional) The mode you want to use to create a bot. Default is CLIENT. The other is APPSERVICE.
    mode: CLIENT
    # (optional) Configure how users managed by your bot do automatically join rooms.
    # ENABLED allows automatic joins to every invited room.
    # DISABLED disables this feature.
    # Default is RESTRICTED, which means, that only automatic joins to serverName are allowed.
    autoJoin: RESTRICTED
  client:
    homeServer:
      # The hostname of your Homeserver.
      hostname: matrix.example.org
      # (optional) The port of your Homeserver. Default is 443.
      port: 443
      # (optional) Use http or https. Default is true (so uses https).
      secure: true
    # The token to authenticate against the Homeserver.
    token: superSecretMatrixToken
  # Properties under appservice are only relevant if you use bot mode APPSERVICE.
  appservice:
      # A unique token for Homeservers to use to authenticate requests to application services.
      hsToken: superSecretHomeserverToken
      # A list of users, aliases and rooms namespaces that the application service controls.
      namespaces:
        users:
            # (optional) A true or false value stating whether this application service 
            # has exclusive access to events within this namespace. Default is true.
          - exclusive: true
            # A regular expression defining which values this namespace includes.
            # Note that this is not similar to the matrix homeserver appservice config,
            # because this regex only regards the username and not user id.
            regex: "_superAppservice_.*"
        aliases:
          - regex: "_superAppservice_.*"
        rooms: []
```

See also the [matrix application service spec](https://matrix.org/docs/spec/application_service/r0.1.2#registration) for more information about the properties defined under `appservice`.

### Persistence

### Bot modes
There are two modes how you can run your bot. The `CLIENT` mode simply acts as a matrix user client. This allows you to create bots without an additional configuration on the Homerserver. The `APPSERVICE` mode acts as a matrix appservice, which allows more customized bots.

#### Client mode
The `CLIENT` mode does sync endless to the Homeserver as soon as you start your application. To manually stop and start the sync to the Homeserver you can autowire [`MatrixClientBot`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/client/MatrixClientBot.kt)

By default, this framework does not persist anything. It is recommended to persist the sync batch token by implementing [`SyncBatchTokenService`](./matrix-spring-boot-rest-client/src/main/kotlin/net/folivo/matrix/restclient/api/sync/SyncBatchTokenService.kt).

#### Appservice mode
To customize the default behaviour of (and add persistence to) the `APPSERVICE` mode you may override [`DefaultMatrixAppserviceEventService`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/appservice/DefaultMatrixAppserviceEventService.kt),  [`DefaultMatrixAppserviceRoomService`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/appservice/DefaultMatrixAppserviceRoomService.kt) and/or [`DefaultMatrixAppserviceUserService`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/appservice/DefaultMatrixAppserviceUserService.kt) and make them available as bean (annotate it with `@Component`). This allows you to control which and how users and rooms should be created and events are handled.

### Handle messages
Just implement [`MatrixMessageContentHandler`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/handler/MatrixMessageContentHandler.kt) and make it available as bean (annotate it with `@Component`). This allows you to react and answer to all Message Events from any room, that you joined.

## Advanced usage

#### Handle all incoming events
Implement [`MatrixEventHandler`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/handler/MatrixEventHandler.kt) and make it available as bean (annotate it with `@Component`). This allows you to react to every Event from any room, that you joined.

#### Interact with Homeserver
A Bean of type [`MatrixClient`](./matrix-spring-boot-rest-client/src/main/kotlin/net/folivo/matrix/restclient/MatrixClient.kt) is created, which can be autowired and used to interact with the matrix API. Currently not all endpoints of the [Client-Server API](https://matrix.org/docs/spec/client_server/r0.6.0) are implemented.

#### Customize auto join behaviour

You can customize the auto join behaviour by implementing [`AutoJoinService`](./matrix-spring-boot-bot/src/main/kotlin/net/folivo/matrix/bot/appservice/event/AutoJoinService.kt). Make it available as bean (annotate it with `@Component`).

## Examples

The module [matrix-spring-boot-bot-examples](./matrix-spring-boot-bot-examples) contains some examples how to use this framework in practice.

Copy the `application.yml.example` file and save it at the same place as `application.yml`. You eventually need to modify the matrix-properties in that file.
