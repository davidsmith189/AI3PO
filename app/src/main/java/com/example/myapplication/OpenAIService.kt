package com.example.myapplication


import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class OpenAIService {
    private val client = OkHttpClient()
    private val apiKey = "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"
    private val apiUrl = "https://api.openai.com/v1/chat/completions"

    fun sendMessage(message: String, callback: (String) -> Unit) {
        val json = JSONObject().apply {
            put("model", "gpt-4o")
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            }))
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

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
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val reply = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    callback(reply.trim())
                } else {
                    callback("Error: Empty response")
                }
            }
        })
    }
}

