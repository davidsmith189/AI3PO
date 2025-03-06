package com.example.myapplication

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

object JsonUtils {
    fun loadJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun getRandomResponse(context: Context): String {
        val jsonString = loadJsonFromAssets(context, "samples.json") ?: return "Oops! No response found."
        val jsonObject = JSONObject(jsonString)
        val responsesArray = jsonObject.getJSONArray("responses")
        val randomIndex = (0 until responsesArray.length()).random()
        return responsesArray.getString(randomIndex)
    }
}
