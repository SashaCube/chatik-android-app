package com.chatik.android.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.net.ConnectException
import java.nio.charset.StandardCharsets.UTF_8

@KtorExperimentalAPI
class MainActivity : AppCompatActivity() {

    private val messages = mutableListOf<String>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: View
    private lateinit var messageAdapter: MessageAdapter

    private val sessionManager = SessionManager()

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListeners()

        coroutineScope.launch {
            if (!sessionManager.connect {
                    initMessageReceiver()
                }) {
                handleException(ConnectException())
            }

            scrollToTheTop()
        }
    }

    private fun scrollToTheTop() {
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recyclerView)
        sendButton = findViewById(R.id.sendBtnLayout)
        inputEditText = findViewById(R.id.etChatInput)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            messageAdapter = MessageAdapter()
            adapter = messageAdapter
        }
    }

    private fun initListeners() {
        sendButton.setOnClickListener {
            send()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    send()
                    true
                }
                else -> false
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.initMessageReceiver() =
        withContext(Dispatchers.IO) {
            incoming.consumeEach {
                if (it.frameType == FrameType.TEXT) {
                    messages.add(it.data.toString(UTF_8))

                    coroutineScope.launch {
                        messageAdapter.setMessages(messages)
                        scrollToTheTop()
                    }
                }
            }
        }

    /**
     * This function handles any error in this Activity
     */
    private fun handleException(e: java.lang.Exception) {
        Log.e(TAG, "exception: $e")
        when (e) {
            is ConnectException -> showLongToast("Failed to connect")
            else -> showLongToast("Error occurred")
        }
    }

    /**
     * Show long toast with [text] using [coroutineScope]
     */
    private fun showLongToast(text: String) {
        coroutineScope.launch {
            Toast.makeText(
                applicationContext,
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun send() {
        coroutineScope.launch {
            val text = inputEditText.text.trim().toString()

            if (text.isNotEmpty()) {
                sessionManager.getSession()?.let { session ->
                    session.send(Frame.Text(text))
                    inputEditText.setText("")
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}