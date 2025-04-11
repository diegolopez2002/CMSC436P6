package com.example.project6

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        textView = findViewById(R.id.textView)

        // Fetch initial word list
        CoroutineScope(Dispatchers.Main).launch {
            val jsonString = ServerTaskJson().fetchJson("http://cmsc436-J2301.cs.umd.edu/project6Json.php")
            Log.d("MainActivity", "Raw JSON response: $jsonString")
            val words = parseWordList(jsonString)
            if (words.isNotEmpty()) {
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, words)
                autoCompleteTextView.setAdapter(adapter)
            } else {
                Log.e("MainActivity", "No words loaded from server")
                autoCompleteTextView.setAdapter(null)
            }
        }

        // Set up click listener for autocomplete
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedWord = autoCompleteTextView.adapter.getItem(position) as String
            fetchMessage(selectedWord)
        }
    }

    private fun fetchMessage(word: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val jsonString = ServerTaskRead().fetchMessage("http://cmsc436-2301.cs.umd.edu/project6Message.php?message=$word")
            Log.d("MainActivity", "Message JSON response: $jsonString")
            val (comment, color) = parseMessageResponse(jsonString)
            updateTextView(comment, color)
        }
    }

    private fun parseWordList(jsonString: String): Array<String> {
        return try {
            if (jsonString.isEmpty()) {
                Log.e("MainActivity", "Empty JSON string received")
                emptyArray()
            } else {
                val jsonArray = JSONArray(jsonString)
                Array(jsonArray.length()) { i -> jsonArray.getString(i) }
            }
        } catch (e: JSONException) {
            Log.e("MainActivity", "JSON parsing error: ${e.message}")
            emptyArray()
        }
    }

    private fun parseMessageResponse(jsonString: String): Pair<String, String> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val found = jsonObject.getString("found") == "yes"
            if (!found) return Pair("NA", "gray")

            val dataArray = jsonObject.getJSONArray("data")
            Pair(dataArray.getString(0), dataArray.getString(1))
        } catch (e: JSONException) {
            Log.e("MainActivity", "Message JSON parsing error: ${e.message}")
            Pair("NA", "gray")
        }
    }

    private fun updateTextView(comment: String, color: String) {
        textView.text = comment
        val backgroundColor = when (color.lowercase()) {
            "yellow" -> android.graphics.Color.YELLOW
            "green" -> android.graphics.Color.GREEN
            "blue" -> android.graphics.Color.BLUE
            "red" -> android.graphics.Color.RED
            "gray" -> android.graphics.Color.GRAY
            else -> android.graphics.Color.GRAY
        }
        textView.setBackgroundColor(backgroundColor)
    }
}
