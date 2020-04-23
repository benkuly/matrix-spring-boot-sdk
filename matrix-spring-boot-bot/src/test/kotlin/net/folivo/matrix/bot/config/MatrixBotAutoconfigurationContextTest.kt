package net.folivo.matrix.bot.config

class MatrixBotAutoconfigurationContextTest {

// TODO find a way to get this running
//    private val contextRunner = ApplicationContextRunner()
//            .withConfiguration(
//                    AutoConfigurations.of(
//                            MatrixBotAutoconfiguration::class.java,
//                            MatrixBotAutoconfigurationContextTestConfiguration::class.java
//                    )
//            )
//
//    @Configuration
//    class MatrixBotAutoconfigurationContextTestConfiguration {
//        @Bean
//        fun matrixClient(): MatrixClient {
//            val mockMatrixClient = mockk<MatrixClient>()
//            val syncApiMock = mockk<SyncApiClient>()
//            every { mockMatrixClient.syncApi }.returns(syncApiMock)
//            every { syncApiMock.syncLoop() }.returns(Flux.empty())
//            return mockMatrixClient
//        }
//
//        @Bean
//        fun syncBatchTokenRepository(): SyncBatchTokenRepository {
//            return mockk<SyncBatchTokenRepository>()
//        }
//    }
//
//    @Test
//    fun `default services`() {
//        this.contextRunner
//                .withPropertyValues("matrix.homeServer.hostname=localhost", "matrix.token=test")
//                .run { context: AssertableApplicationContext ->
//                    Assertions.assertThat(context).hasSingleBean(MatrixBot::class.java)
//                    Assertions.assertThat(context).hasSingleBean(MatrixMessageEventHandler::class.java)
//                    Assertions.assertThat(context).hasSingleBean(PersistenceSyncBatchTokenService::class.java)
//                }
//    }
}