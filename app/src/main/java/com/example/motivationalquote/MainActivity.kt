package com.example.motivationalquote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import com.example.motivationalquote.ui.theme.MotivationalQuoteTheme

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

val categories = listOf(
    "age", "alone", "amazing", "anger", "architecture", "art", "attitude", "beauty", "best",
    "birthday", "business", "car", "change", "communication", "computers", "cool", "courage",
    "dad", "dating", "death", "design", "dreams", "education", "environmental", "equality",
    "experience", "failure", "faith", "family", "famous", "fear", "fitness", "food", "forgiveness",
    "freedom", "friendship", "funny", "future", "god", "good", "government", "graduation", "great",
    "happiness", "health", "history", "home", "hope", "humor", "imagination", "inspirational",
    "intelligence", "jealousy", "knowledge", "leadership", "learning", "legal", "life", "love",
    "marriage", "medical", "men", "mom", "money", "morning", "movies", "success"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotivationalQuoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RenewQuote()
                }
            }
        }
    }
}

private suspend fun fetchQuote(category: String, callback: (String) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.api-ninjas.com/v1/quotes?category=$category"
            val url = URL(apiUrl)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("X-Api-Key", "ufRtxnNVzIGC/PWm08KxDg==DkJ44HWdhxzwoIXt")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(response.toString())
                val jsonObject = jsonArray.getJSONObject(0)
                val quote = jsonObject.getString("quote")
                val author = jsonObject.getString("author")
                callback("$quote\n- $author")
            } else {
                val errorStream = connection.errorStream
                val errorReader = BufferedReader(InputStreamReader(errorStream))
                val errorResponse = errorReader.readText()
                errorReader.close()
                callback("Error: $responseCode, $errorResponse")
            }
        } catch (e: Exception) {
            callback("Failed to load quote: ${e.message}")
        }
    }
}

private fun getRandomCategory(): String {
    return categories.random()
}


@Composable
fun QuoteDisplay(quote: String, author: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Quote text, left-aligned
        Text(
            text = quote,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )

        // Author text, right-aligned
        Text(
            text = "- $author",
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RenewQuote() {
    var quote by remember { mutableStateOf("Fetching quote...") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(getRandomCategory()) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch the initial quote when the composable is first launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fetchQuote(category) { fetchedQuote ->
                val quoteParts = fetchedQuote.split("\n- ") // Split quote and author
                quote = quoteParts[0]
                author = quoteParts[1]
            }
        }
    }

    // Display the quote and the button
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        QuoteDisplay(quote = quote, author = author)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            category = getRandomCategory()
            coroutineScope.launch {
                fetchQuote(category) { fetchedQuote ->
                    val quoteParts = fetchedQuote.split("\n- ")
                    quote = quoteParts[0]
                    author = quoteParts[1]
                }
            }
        }) {
            Text(text = "Renew Quote")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun QuoteDisplayPreview() {
    MotivationalQuoteTheme {
        RenewQuote()
    }
}