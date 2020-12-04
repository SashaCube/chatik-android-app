package com.chatik.android.app

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.isActive

/**
 * Class that handle web socket session
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class SessionManager {

    private var client: HttpClient = HttpClient(OkHttp) {
        install(WebSockets)
    }
    private var session: WebSocketSession? = null

    private fun isConnected(): Boolean {
        return session?.isActive ?: false
    }

    /**
     * Connects to the server.
     * Returns true if connected and false if some error occurred
     */
    suspend fun connect(block: suspend DefaultClientWebSocketSession.() -> Unit): Boolean {
        return try {
            client.ws(
                method = HttpMethod.Get,
                host = "10.0.2.2",
                port = 8080,
                path = "/ws"
            ) {
                session = this
                block()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Disconnects from the server.
     * Returns true if disconnected and false if some error occurred
     */
    suspend fun disconnect(): Boolean {
        return session?.let {
            if (it.isActive) {
                it.close()
                true
            } else {
                false
            }
        } ?: false
    }

    /**
     * This function returns web socket session if it's active
     */
    fun getSession() = if (isConnected()) session else null

    companion object {
        private const val TAG = "SessionManager"
    }
}