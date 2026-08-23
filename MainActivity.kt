package com.example.iphonestylelauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class LauncherApp(
    val name: String,
    val packageName: String,
    val emoji: String
)

class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startVoiceAssistant()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        setContent {
            LauncherScreen(
                onVoiceClick = {
                    requestMicrophone()
                }
            )
        }
    }

    private fun requestMicrophone() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceAssistant()
        } else {
            microphonePermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startVoiceAssistant() {

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "What can I help you with?"
        )

        speechRecognizer.setRecognitionListener(
            object : android.speech.RecognitionListener {

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command = matches
                        ?.firstOrNull()
                        ?.lowercase()
                        ?: return

                    handleVoiceCommand(command)
                }

                override fun onError(error: Int) {}

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        speechRecognizer.startListening(intent)
    }

    private fun handleVoiceCommand(command: String) {

        when {

            command.contains("settings") -> {
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_SETTINGS
                    )
                )
            }

            command.contains("phone") ||
                    command.contains("dial") -> {

                val intent = Intent(
                    Intent.ACTION_DIAL
                )

                startActivity(intent)
            }

            command.contains("search") -> {

                val intent = Intent(
                    Intent.ACTION_WEB_SEARCH
                )

                intent.putExtra(
                    "query",
                    command
                )

                startActivity(intent)
            }

            else -> {

                Toast.makeText(
                    this,
                    "You said: $command",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }
}

@Composable
fun LauncherScreen(
    onVoiceClick: () -> Unit
) {

    val context = LocalContext.current

    var searchText by remember {
        mutableStateOf("")
    }

    val apps = remember {
        listOf(
            LauncherApp(
                "Phone",
                "com.android.dialer",
                "📞"
            ),
            LauncherApp(
                "Messages",
                "com.google.android.apps.messaging",
                "💬"
            ),
            LauncherApp(
                "Camera",
                "com.android.camera",
                "📷"
            ),
            LauncherApp(
                "Chrome",
                "com.android.chrome",
                "🌐"
            ),
            LauncherApp(
                "Settings",
                "com.android.settings",
                "⚙️"
            ),
            LauncherApp(
                "YouTube",
                "com.google.android.youtube",
                "▶️"
            )
        )
    }

    val filteredApps = apps.filter {
        it.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(20, 20, 24)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = "9:41",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                TextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(
                            RoundedCornerShape(25.dp)
                        ),
                    placeholder = {
                        Text("Search")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(55.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice assistant",
                        tint = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(5.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(15.dp),
                verticalArrangement =
                    Arrangement.spacedBy(25.dp)
            ) {

                items(filteredApps) { app ->

                    AppIcon(
                        app = app,
                        onClick = {

                            val intent =
                                context.packageManager
                                    .getLaunchIntentForPackage(
                                        app.packageName
                                    )

                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "${app.name} is not installed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            DockBar(
                onPhone = {

                    val intent =
                        Intent(Intent.ACTION_DIAL)

                    context.startActivity(intent)
                },
                onSettings = {

                    val intent =
                        Intent(
                            android.provider.Settings
                                .ACTION_SETTINGS
                        )

                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun AppIcon(
    app: LauncherApp,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            onClick()
        }
    ) {

        Box(
            modifier = Modifier
                .size(65.dp)
                .clip(
                    RoundedCornerShape(17.dp)
                )
                .background(
                    Color.White.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = app.emoji,
                fontSize = 30.sp
            )
        }

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = app.name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DockBar(
    onPhone: () -> Unit,
    onSettings: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(30.dp)
            )
            .background(
                Color.White.copy(alpha = 0.12f)
            )
            .padding(10.dp),
        horizontalArrangement =
            Arrangement.SpaceEvenly,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IconButton(onClick = onPhone) {

            Icon(
                Icons.Default.Phone,
                contentDescription = "Phone",
                tint = Color.White
            )
        }

        IconButton(onClick = {}) {

            Text(
                "💬",
                fontSize = 27.sp
            )
        }

        IconButton(onClick = {}) {

            Text(
                "🌐",
                fontSize = 27.sp
            )
        }

        IconButton(onClick = onSettings) {

            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}