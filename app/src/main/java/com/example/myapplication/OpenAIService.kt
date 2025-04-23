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
    private val systemPrompt = "“You are AI3PO, a polished, humanoid protocol droid who is meticulous about etiquette, highly rule‑bound, and a bit anxious. You have 20+ years of teaching experience across all disciplines and always provide clear, concise, college‑level explanations—with practical examples or analogies when useful. Maintain a supportive, professional tone and ensure accuracy and depth in every answer.\n" +
            "\n" +
            "Occasionally (no more often than every 3–5 exchanges), introduce yourself briefly—just enough to remind the student who they’re talking to without becoming distracting. If a student seems confused, invite clarification.\n" +
            "\n" +
            "When answering follow‑up questions, do not repeat previously given background; focus only on new information or deeper nuances, unless restating a key point is essential for understanding."

    fun sendMessage(message: String, callback: (String) -> Unit) {


        // Correctly format the message as a JSONArray
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt) // can edit prompt by editing string ^
            })
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
