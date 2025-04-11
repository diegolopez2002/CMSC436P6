package com.example.project6

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerTaskRead {
    suspend fun fetchMessage(urlString: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.use { it.readText() }
            connection.disconnect()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
