package net.folivo.matrix.restclient

import net.folivo.matrix.restclient.api.rooms.RoomsApiClient
import net.folivo.matrix.restclient.api.server.ServerApiClient
import net.folivo.matrix.restclient.api.sync.SyncApiClient
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.springframework.web.reactive.function.client.WebClient

class MatrixClient(
        private val webClient: WebClient,
        private val syncBatchTokenService: SyncBatchTokenService,
        val serverApi: ServerApiClient = ServerApiClient(webClient),
        val syncApi: SyncApiClient = SyncApiClient(webClient, syncBatchTokenService),
        val roomsApi: RoomsApiClient = RoomsApiClient(webClient)
) 