package com.example.myapplication

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.myapplication.BuildConfig

class OpenAIService {
    private val client = OkHttpClient()
    private val apiKey = "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"
    private val apiUrl = "https://api.openai.com/v1/chat/completions"

    fun sendMessage(message: String, callback: (String) -> Unit) {
        // Correctly format the message as a JSONArray
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })
        }

        // Construct the JSON payload
        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messagesArray)
            put("max_tokens", 150)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    try {
                        val jsonResponse = JSONObject(it)

                        // Check for errors in the response before trying to parse choices
                        if (!jsonResponse.has("choices")) {
                            callback("Error: ${jsonResponse.toString(2)}")
                            return
                        }

                        val reply = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        callback(reply.trim())
                    } catch (e: Exception) {
                        callback("Error parsing response: ${e.message}")
                    }
                } ?: callback("Error: Empty response")
            }
        })
    }
}
