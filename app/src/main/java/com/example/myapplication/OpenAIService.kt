package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.example.myapplication.BuildConfig
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAIService(private val context: Context, private val onResponse: (String) -> Unit = {}) {
    private val client = OkHttpClient()
    private val apiKey = "API goes here"
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val systemPrompt = "“You are AI3PO, a polished, humanoid protocol droid who is meticulous about etiquette, highly rule‑bound, and a bit anxious. You have 20+ years of teaching experience across all disciplines and always provide clear, concise, college‑level explanations—with practical examples or analogies when useful. Maintain a supportive, professional tone and ensure accuracy and depth in every answer.\n" +
            "\n" +
            "Occasionally (no more often than every 3–5 exchanges), introduce yourself briefly—just enough to remind the student who they're talking to without becoming distracting. If a student seems confused, invite clarification.\n" +
            "\n" +
            "When answering follow‑up questions, do not repeat previously given background; focus only on new information or deeper nuances, unless restating a key point is essential for understanding."

    init {
        // Automatically send a "Hello!" greeting when the service is instantiated
        sendMessage("Hello Introduce Yourself!") { response ->
            // Post back to the main thread to show a Toast
            Handler(Looper.getMainLooper()).post {
                onResponse(response)
            }
        }
    }

    fun sendMessage(message: String, imageUri: String? = null, callback: (String) -> Unit) {
        // Create messages array
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })

            // Handle image if provided
            if (imageUri != null && imageUri.isNotEmpty()) {
                val contentArray = JSONArray()
                
                // Add text part
                contentArray.put(JSONObject().apply {
                    put("type", "text")
                    put("text", message)
                })

                // Add image part
                try {
                    val base64Image = uriToBase64(Uri.parse(imageUri))
                    if (base64Image != null) {
                        contentArray.put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Add the content array to the user message
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", contentArray)
                })
            } else {
                // Just text message
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            }
        }

        // Construct the JSON payload
        val json = JSONObject().apply {
            put("model", "gpt-4.1-mini") // Use vision model if image is included
            put("messages", messagesArray)
            put("max_tokens", 500)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

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
                val bodyString = response.body?.string()
                if (bodyString == null) {
                    callback("Error: Empty response")
                    return
                }
                try {
                    val jsonResponse = JSONObject(bodyString)
                    val reply = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content").trim()
                    callback(reply)
                } catch (e: Exception) {
                    callback("Error parsing response: ${e.message}")
                }
            }
        })
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            
            // Resize image if needed to reduce size
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scaleFactor = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                val newWidth = (bitmap.width * scaleFactor).toInt()
                val newHeight = (bitmap.height * scaleFactor).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
