package com.example.flashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.flashcard.ui.theme.OroPicReadTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OroPicReadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageListFromURL()
                }
            }
        }
    }
}

@Composable
fun ImageListFromURL() {
    val context = LocalContext.current
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchDataFromServer(context) { data ->
            flashcards = parseFlashcards(data)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        flashcards.forEach { flashcard ->
            FlashcardItem(flashcard = flashcard)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FlashcardItem(flashcard: Flashcard) {
    var isFront by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isFront = !isFront },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFront) {
                // Display the image on the front
                ImageFromURL(imageUrl = flashcard.image, description = "Tap to see the back")
            } else {
                // Display the text on the back
                Text(
                    text = flashcard.back,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ImageFromURL(imageUrl: String, description: String) {
    val painter = rememberAsyncImagePainter(model = imageUrl)
    Image(
        painter = painter,
        contentDescription = description,
        modifier = Modifier.size(200.dp)
    )
}

private suspend fun fetchDataFromServer(context: android.content.Context, callback: (String) -> Unit) {
    val url = "http://192.168.149.76:5555/read_data"
    val data = withContext(Dispatchers.IO) {
        URL(url).readText()
    }
    callback(data)
}

private fun parseFlashcards(data: String): List<Flashcard> {
    val flashcardList = mutableListOf<Flashcard>()
    try {
        val jsonObject = JSONObject(data)
        val dataArray = jsonObject.getJSONArray("data")
        for (i in 0 until dataArray.length()) {
            val itemObject = dataArray.getJSONObject(i)
            val name = itemObject.getString("name")
            val description = itemObject.getString("description")
            val image = itemObject.getString("image")
            if (description.isNotEmpty() && image.isNotEmpty()) {
                flashcardList.add(Flashcard(image, description))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return flashcardList
}

data class Flashcard(val image: String, val back: String)
