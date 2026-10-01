package com.nexoai.keyboard

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AiApiClient(context: Context) {
    private val config = ConfigStore(context.applicationContext)

    fun transform(
        text: String,
        mode: AiMode,
        contextText: String = "",
        customInstruction: String = ""
    ): Result<String> = runCatching {
        val endpoint = config.endpoint
        require(endpoint.startsWith("https://")) { "O endpoint precisa usar HTTPS." }
        require(config.appToken.isNotBlank()) { "Configure o APP_TOKEN no Nexo AI." }
        require(text.isNotBlank()) { "Não há texto para processar." }
        require(text.length <= config.maxChars) { "Texto acima do limite de ${config.maxChars} caracteres." }

        val body = JSONObject().apply {
            put("text", text)
            put("mode", mode.code)
            put("context", contextText)
            put("style", config.personalStyle)
            put("length", config.outputLength)
            put("max_output_chars", config.outputMaxChars)
            if (customInstruction.isNotBlank()) put("custom_instruction", customInstruction)
        }.toString()

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 65000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-App-Token", config.appToken)
            setRequestProperty("User-Agent", "NexoAI-Keyboard/3.1 Android")
        }

        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val responseText = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
        connection.disconnect()

        val json = JSONObject(responseText.ifBlank { "{}" })
        if (status !in 200..299 || !json.optBoolean("ok", false)) {
            throw IllegalStateException(json.optString("error", "Falha HTTP $status"))
        }
        json.optString("text").trim().ifBlank { throw IllegalStateException("A IA não retornou texto.") }
    }
}
