package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.database.Holding
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

// --- High Density Theme Palette (Dynamic M3) ---
var isDarkThemeState by mutableStateOf(false)

val CanvasBg: Color @Composable get() = if (isDarkThemeState) Color(0xFF0F1115) else Color(0xFFFDFBFF)
val PanelBg: Color @Composable get() = if (isDarkThemeState) Color(0xFF16181D) else Color(0xFFFFFFFF)
val AccentMint: Color @Composable get() = if (isDarkThemeState) Color(0xFF4ADE80) else Color(0xFF386A20)
val AccentBlue: Color @Composable get() = if (isDarkThemeState) Color(0xFF60A5FA) else Color(0xFF0061A4)
val AccentOrange: Color @Composable get() = if (isDarkThemeState) Color(0xFFF87171) else Color(0xFFBA1A1A)
val BorderColor: Color @Composable get() = if (isDarkThemeState) Color(0xFF2C2E35) else Color(0xFFDDE2EA)
val TextGray: Color @Composable get() = if (isDarkThemeState) Color(0xFF94A3B8) else Color(0xFF44474E)
val BrandNavy: Color @Composable get() = if (isDarkThemeState) Color(0xFFE2E8F0) else Color(0xFF001D36)
val ContentTextColor: Color @Composable get() = if (isDarkThemeState) Color(0xFFF1F5F9) else Color(0xFF1A1C1E)

@Composable
fun ThemeToggle(modifier: Modifier = Modifier, tag: String = "action_toggle_theme") {
    IconButton(
        onClick = { isDarkThemeState = !isDarkThemeState },
        modifier = modifier
            .background(PanelBg, CircleShape)
            .border(1.dp, BorderColor.copy(alpha = 0.5f), CircleShape)
            .size(36.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = if (isDarkThemeState) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle Theme",
            tint = if (isDarkThemeState) Color(0xFFFBBF24) else (if (isDarkThemeState) Color.White else BrandNavy),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val actualBorderColor = borderColor ?: BorderColor
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, actualBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = PanelBg),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

// ==========================================
// 1. DYNAMIC RADAR & DAILY AI ALPHA SCREEN
// ==========================================
@Composable
fun RadarScreen(viewModel: FinancialViewModel) {
    val alphaReport by viewModel.alphaReport.collectAsState()
    val sectorRotations by viewModel.sectorRotations.collectAsState()
    val radarBriefing by viewModel.radarBriefing.collectAsState()
    val isRefreshing by viewModel.isRefreshingRadar.collectAsState()

    val context = LocalContext.current
    val voiceQuestion by viewModel.voiceQuestion.collectAsState()
    val voiceAnswer by viewModel.voiceAnswer.collectAsState()
    val isVoiceProcessing by viewModel.isVoiceProcessing.collectAsState()
    val voiceError by viewModel.voiceError.collectAsState()

    var isListening by remember { mutableStateOf(false) }
    var transcriptionText by remember { mutableStateOf("") }
    var listeningStateText by remember { mutableStateOf("Tap the mic & ask about your portfolio or market trends.") }

    val speechRecognizer = remember {
        try {
            SpeechRecognizer.createSpeechRecognizer(context)
        } catch (e: Exception) {
            null
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listeningStateText = "Listening... Speak now."
            }

            override fun onBeginningOfSpeech() {
                listeningStateText = "Recording audio..."
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                listeningStateText = "Transcribing your voice query..."
            }

            override fun onError(error: Int) {
                isListening = false
                listeningStateText = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition not active. Try quick links."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue. Speak query or use links."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap and try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy. Please retry."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Please speak."
                    else -> "Voice recognition unavailable. Use quick suggestions."
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val queryText = matches[0]
                    transcriptionText = queryText
                    listeningStateText = "Transcribed successfully!"
                    viewModel.processVoiceQuestion(queryText)
                } else {
                    listeningStateText = "Could not recognize query. Try again."
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            transcriptionText = ""
            listeningStateText = "Initializing microphone..."
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                listeningStateText = "Could not open voice engine: ${e.message}"
            }
        } else {
            listeningStateText = "Permission required to transcribe your voice."
        }
    }

    val toggleListening = {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            listeningStateText = "Mic suspended."
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                isListening = true
                transcriptionText = ""
                listeningStateText = "Initializing microphone..."
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    speechRecognizer?.startListening(intent)
                } catch (e: Exception) {
                    isListening = false
                    listeningStateText = "Speech engine error: ${e.message}"
                }
            } else {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Header (Custom Design matching HTML)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SHARROW.AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Good Morning, Sudhakar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeToggle(tag = "action_toggle_theme_radar")
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFD1E4FF), CircleShape)
                            .border(1.dp, Color(0xFF74777F).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SK",
                            color = Color(0xFF001D36),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Feature 1 + 11: Daily AI Alpha Report Briefing
        item {
            GlowCard(
                borderColor = AccentMint.copy(alpha = 0.3f),
                modifier = Modifier.testTag("ai_market_radar_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, "Alpha Report", tint = AccentMint)
                            Text(
                                "DAILY AI ALPHA BRIEF",
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.refreshIntelligenceBriefing() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(color = AccentMint, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.Refresh, "Refresh", tint = AccentMint, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Good Morning Sudhakar,",
                        color = ContentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = alphaReport?.briefing ?: "Loading market insights from Sharrow.ai neural engine...",
                        color = TextGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Divider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Market Sentiment", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(alphaReport?.sentiment ?: "Bullish", color = AccentMint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Top Opportunity", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(alphaReport?.topOpportunity ?: "BEL", color = ContentTextColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Risk Profile", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(alphaReport?.highestRisk ?: "Financials", color = AccentOrange, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F0FF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFADC6FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Takeaway: " + (alphaReport?.takeaways ?: "Checking macro sectors..."),
                            color = Color(0xFF004786),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Stock Performance History Chart (30 Days)
        item {
            StockPerformanceLineChartCard()
        }

        // AI Voice Research Copilot component
        item {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            GlowCard(
                borderColor = if (isListening) AccentOrange.copy(alpha = 0.8f) else AccentBlue.copy(alpha = 0.3f),
                modifier = Modifier.testTag("ai_voice_copilot_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Copilot",
                                tint = if (isListening) AccentOrange else AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "AI VOICE RESEARCH ASSISTANT",
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                fontSize = 13.sp
                            )
                        }

                        // Status pill badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isListening) AccentOrange.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isListening) "LISTENING" else "READY",
                                color = if (isListening) AccentOrange else TextGray,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Microphone Activation Circle Button
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            // Pulsing Ring under active listening state
                            if (isListening) {
                                val pulseColor = AccentOrange
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .drawBehind {
                                            drawCircle(
                                                color = pulseColor,
                                                radius = (size.minDimension / 2f) * pulseScale,
                                                alpha = pulseAlpha
                                            )
                                        }
                                )
                            }

                            // Interactive Circle Button
                            IconButton(
                                onClick = { toggleListening() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening) AccentOrange else AccentBlue)
                                    .testTag("action_toggle_voice_mic")
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.Close else Icons.Default.Mic,
                                    contentDescription = "Microphone Trigger",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = listeningStateText,
                            color = if (isListening) AccentOrange else TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Voice Conversation Panel (Question and Response)
                    if (voiceQuestion.isNotEmpty()) {
                        Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 12.dp))

                        // User Question Bubble
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBox, "User", tint = TextGray, modifier = Modifier.size(16.dp))
                            Text(
                                text = "You: \"$voiceQuestion\"",
                                color = ContentTextColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.testTag("voice_copilot_question")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Response Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEEF6FF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFADC6FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SHARROW.AI RESPONSE", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    if (isVoiceProcessing) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AccentBlue)
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.clearVoiceState() },
                                            modifier = Modifier.size(16.dp).testTag("action_clear_voice")
                                        ) {
                                            Icon(Icons.Default.Clear, "Clear", tint = TextGray.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (isVoiceProcessing) {
                                    Text(
                                        "Analyzing transaction buffers and sector risk alignments...",
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                } else if (voiceError.isNotEmpty()) {
                                    Text(
                                        "Error: $voiceError",
                                        color = AccentOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = voiceAnswer,
                                        color = ContentTextColor,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.testTag("voice_copilot_response")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggestion Links for quick evaluation
                    Text(
                        "TAP A SUGGESTED VOICE QUERY:",
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val quickQueries = listOf(
                        "Analyze my portfolio risk factors",
                        "Is BEL a good defensive buy?",
                        "What is my net portfolio valuation?",
                        "What are the top IT sector catalysts?"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickQueries.forEach { query ->
                            SuggestionChip(
                                onClick = {
                                    transcriptionText = query
                                    listeningStateText = "Applying quick link suggestion..."
                                    viewModel.processVoiceQuestion(query)
                                },
                                label = { Text(query, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = BrandNavy
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = BorderColor.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.testTag("quick_voice_query_${query.replace(" ", "_")}")
                            )
                        }
                    }
                }
            }
        }

        // Feature 8: Sector Rotation Tracker
        item {
            Text(
                "SECTOR ROTATION TRACKER",
                style = MaterialTheme.typography.titleSmall,
                color = BrandNavy,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(sectorRotations) { strength ->
            GlowCard(modifier = Modifier.testTag("sector_strength_${strength.sector}")) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(strength.sector, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(strength.institutionalFlow, color = TextGray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val trendColor = when (strength.trend) {
                                "UP_FAST" -> AccentMint
                                "UP" -> AccentMint.copy(alpha = 0.7f)
                                "DOWN" -> AccentOrange.copy(alpha = 0.7f)
                                else -> AccentOrange
                            }
                            val arrowIcon = when (strength.trend) {
                                "UP_FAST" -> "↑↑"
                                "UP" -> "↑"
                                "DOWN" -> "↓"
                                else -> "↓↓"
                            }
                            Text(
                                text = arrowIcon,
                                color = trendColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.testTag("sector_trend_arrow")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(trendColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${(strength.score * 100).toInt()}%",
                                    color = trendColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strength.explanation, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { strength.score },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (strength.score >= 0.5f) AccentMint else AccentOrange,
                        trackColor = BorderColor
                    )
                }
            }
        }

        // Feature 10: AI Mentor Educational Coach
        item {
            GlowCard(borderColor = AccentBlue.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, "Investing Coach", tint = AccentBlue)
                        Text(
                            "AI COACHING SEGMENT",
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Why High IT Concentration Risks Your Returns",
                        color = ContentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Holding over 45% in technology means your portfolio depends entirely on US monetary decisions. If the Fed changes rates, IT multipliers correct quickly. Overcome this by shifting a 15% budget to non-cyclical defense ordering like BEL.",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}


data class ChartSlice(
    val symbol: String,
    val value: Double,
    val percentage: Double,
    val color: Color
)

@Composable
fun PortfolioPieChartCard(holdings: List<Holding>) {
    val totalPortfolioValue = holdings.sumOf { it.quantity * it.currentPrice }
    
    val chartColors = listOf(
        Color(0xFF0061A4), // AccentBlue
        Color(0xFF386A20), // AccentMint
        Color(0xFF8B5CF6), // Purple
        Color(0xFFF59E0B), // Amber/Orange
        Color(0xFFEC4899), // Pink
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Light Blue
        Color(0xFFF43F5E), // Rose
        Color(0xFF14B8A6), // Teal
        Color(0xFF84CC16)  // Lime
    )

    val transitionState = remember { Animatable(0f) }
    LaunchedEffect(holdings) {
        transitionState.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    GlowCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PORTFOLIO ASSET DISTRIBUTION",
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                letterSpacing = 1.sp,
                fontSize = 13.sp,
                modifier = Modifier.testTag("portfolio_distribution_title")
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty distribution",
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active holdings to display distribution",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val slices = holdings.mapIndexed { index, holding ->
                    val value = holding.quantity * holding.currentPrice
                    val percentage = if (totalPortfolioValue > 0) (value / totalPortfolioValue) * 100 else 0.0
                    val color = chartColors[index % chartColors.size]
                    ChartSlice(holding.symbol, value, percentage, color)
                }.sortedByDescending { it.value }

                val maxSlices = 5
                val processedSlices = if (slices.size <= maxSlices) {
                    slices
                } else {
                    val topSlices = slices.take(maxSlices - 1)
                    val remainingValue = slices.drop(maxSlices - 1).sumOf { it.value }
                    val remainingPercentage = slices.drop(maxSlices - 1).sumOf { it.percentage }
                    topSlices + ChartSlice(
                        symbol = "Others",
                        value = remainingValue,
                        percentage = remainingPercentage,
                        color = Color(0xFF64748B)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .testTag("portfolio_pie_chart_container"),
                            contentAlignment = Alignment.Center
                        ) {
                            val strokeWidth = 16.dp
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                val gapDegrees = if (processedSlices.size > 1) 1.5f else 0f
                                processedSlices.forEach { slice ->
                                    val rawSweepAngle = (slice.percentage.toFloat() / 100f) * 360f * transitionState.value
                                    val sweepAngle = if (rawSweepAngle > gapDegrees) rawSweepAngle - gapDegrees else rawSweepAngle
                                    
                                    if (sweepAngle > 0) {
                                        drawArc(
                                            color = slice.color,
                                            startAngle = startAngle + (gapDegrees / 2),
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                                        )
                                    }
                                    startAngle += rawSweepAngle
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "₹${String.format("%,.0f", totalPortfolioValue)}",
                                    color = BrandNavy,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Divider(color = BorderColor)

                    // Legends Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        processedSlices.forEach { slice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(slice.color, CircleShape)
                                    )
                                    Text(
                                        text = slice.symbol,
                                        color = ContentTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "₹${String.format("%,.2f", slice.value)}",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(slice.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%.1f", slice.percentage)}%",
                                            color = slice.color,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockPerformanceLineChartCard(
    modifier: Modifier = Modifier
) {
    val stockSymbols = listOf("TCS", "BEL", "HDFC", "RELIANCE", "INFY", "ICICI", "ITC")
    var selectedSymbol by remember { mutableStateOf("TCS") }
    
    val history = remember(selectedSymbol) { get30DayHistory(selectedSymbol) }
    var activeIndex by remember(selectedSymbol) { mutableStateOf<Int?>(null) }
    
    val minPrice = history.minOrNull() ?: 0f
    val maxPrice = history.maxOrNull() ?: 100f
    val startPrice = history.firstOrNull() ?: 0f
    val currentPrice = history.lastOrNull() ?: 0f
    
    val priceDiff = currentPrice - startPrice
    val percentChange = if (startPrice > 0) (priceDiff / startPrice) * 100f else 0f
    
    val isPositive = percentChange >= 0
    val chartColor = if (isPositive) AccentMint else AccentOrange
    
    // Resolve dynamic colors locally to avoid inside-Canvas Composable context errors
    val localBorderColor = BorderColor
    val localTextGray = TextGray
    
    GlowCard(
        borderColor = chartColor.copy(alpha = 0.4f),
        modifier = modifier.testTag("stock_performance_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "30-DAY PERFORMANCE GRAPH (RECHARTS STYLE)",
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${getStockFullName(selectedSymbol)} (${selectedSymbol})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                
                // Direction icon indicating performance direction
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(chartColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Trend direction indicator",
                        tint = chartColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stockSymbols.forEach { symbol ->
                    val isSelected = selectedSymbol == symbol
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSymbol = symbol },
                        label = { Text(symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isPositive) AccentMint.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                            selectedLabelColor = chartColor,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderColor,
                            selectedBorderColor = chartColor,
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.testTag("chip_stock_select_$symbol")
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row / Tooltip readout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF808080).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (activeIndex != null) "SELECTED DAY PRICE" else "CURRENT CLOSING PRICE",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val displayPrice = if (activeIndex != null) history[activeIndex!!] else currentPrice
                    Text(
                        text = "₹${String.format("%,.2f", displayPrice)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandNavy
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (activeIndex != null) "GAIN FROM DAY 1" else "30-DAY TOTAL RETURN",
                        fontSize = 9.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val displayChange = if (activeIndex != null) {
                        val activeVal = history[activeIndex!!]
                        if (startPrice > 0) ((activeVal - startPrice) / startPrice) * 100f else 0f
                    } else percentChange
                    
                    val changeColor = if (displayChange >= 0) AccentMint else AccentOrange
                    val plusSign = if (displayChange >= 0) "+" else ""
                    
                    Text(
                        text = "${plusSign}${String.format("%.2f", displayChange)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Interactive Chart Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val gridLinesCount = 4
                
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column for Y-Axis labels (mimicking Recharts YAxis)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(55.dp)
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        for (j in 0 until gridLinesCount) {
                            val ratio = (gridLinesCount - 1 - j).toFloat() / (gridLinesCount - 1)
                            val priceVal = minPrice + ratio * (maxPrice - minPrice)
                            Text(
                                text = "₹${String.format("%.0f", priceVal)}",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Main Chart Area Container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val width = size.width
                                            val idx = ((offset.x / width) * 29f).roundToInt().coerceIn(0, 29)
                                            activeIndex = idx
                                        },
                                        onDrag = { change, _ ->
                                            val width = size.width
                                            val idx = ((change.position.x / width) * 29f).roundToInt().coerceIn(0, 29)
                                            activeIndex = idx
                                        },
                                        onDragEnd = { /* keep active select to inspect */ },
                                        onDragCancel = { }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = { offset ->
                                            val width = size.width
                                            val idx = ((offset.x / width) * 29f).roundToInt().coerceIn(0, 29)
                                            activeIndex = idx
                                        }
                                    )
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            
                            val heightOffset = 12.dp.toPx()
                            val usableHeight = height - heightOffset * 2f
                            val spacingX = width / 29f
                            
                            // Draw horizontal grid lines (dashed)
                            for (j in 0 until gridLinesCount) {
                                val ratio = j.toFloat() / (gridLinesCount - 1)
                                val gridY = heightOffset + usableHeight - ratio * usableHeight
                                drawLine(
                                    color = localBorderColor.copy(alpha = 0.5f),
                                    start = Offset(0f, gridY),
                                    end = Offset(width, gridY),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }
                            
                            if (history.isNotEmpty()) {
                                val points = history.mapIndexed { index, price ->
                                    val x = index * spacingX
                                    val y = heightOffset + usableHeight - ((price - minPrice) / (maxPrice - minPrice)) * usableHeight
                                    Offset(x, y)
                                }
                                
                                // Build continuous path for Area Fill under line
                                val areaPath = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                    lineTo(points.last().x, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                
                                // Draw Area under the Curve Gradient (Recharts Area Style)
                                drawPath(
                                    path = areaPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            chartColor.copy(alpha = 0.22f),
                                            Color.Transparent
                                        ),
                                        startY = heightOffset,
                                        endY = height
                                    )
                                )
                                
                                // Build high precision continuous path for Line Stroke
                                val linePath = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                
                                // Draw Stroke Line
                                drawPath(
                                    path = linePath,
                                    color = chartColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                
                                // Draw crosshair and active indicator point
                                activeIndex?.let { index ->
                                    if (index in points.indices) {
                                        val activePoint = points[index]
                                        
                                        // Vertical guide line
                                        drawLine(
                                            color = localTextGray.copy(alpha = 0.4f),
                                            start = Offset(activePoint.x, 0f),
                                            end = Offset(activePoint.x, height),
                                            strokeWidth = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                        
                                        // Double node circles
                                        drawCircle(
                                            color = chartColor,
                                            radius = 6.dp.toPx(),
                                            center = activePoint
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 3.dp.toPx(),
                                            center = activePoint
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Floating Tooltip card overlays (clever UX positions: switches side so it never blocks touch)
                        activeIndex?.let { index ->
                            if (index in history.indices) {
                                val tooltipOnLeft = index > 14
                                val alignVal = if (tooltipOnLeft) Alignment.TopStart else Alignment.TopEnd
                                
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = PanelBg),
                                    modifier = Modifier
                                        .align(alignVal)
                                        .padding(8.dp)
                                        .border(1.dp, BorderColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "DAY ${index + 1}",
                                            fontSize = 9.sp,
                                            color = TextGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "₹${String.format("%,.2f", history[index])}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandNavy
                                        )
                                        
                                        val startVal = history[0]
                                        val changeSinceStart = if (startVal > 0) ((history[index] - startVal) / startVal) * 100f else 0f
                                        val col = if (changeSinceStart >= 0) AccentMint else AccentOrange
                                        val sign = if (changeSinceStart >= 0) "+" else ""
                                        
                                        Text(
                                            text = "${sign}${String.format("%.2f", changeSinceStart)}% from Day 1",
                                            fontSize = 9.sp,
                                            color = col,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // X Axis Labels mimicking Recharts XAxis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 55.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Day 1", "Day 10", "Day 20", "Day 30").forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Interactive info label",
                    tint = TextGray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Tap or drag across the chart canvas to inspect precise daily close points.",
                    color = TextGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

fun get30DayHistory(symbol: String): List<Float> {
    val (basePrice, volatility, trend) = when (symbol) {
        "TCS" -> Triple(3950f, 0.012f, 0.002f)
        "BEL" -> Triple(260f, 0.028f, 0.007f)
        "HDFC" -> Triple(1610f, 0.014f, 0.0003f)
        "RELIANCE" -> Triple(2480f, 0.016f, -0.001f)
        "INFY" -> Triple(1850f, 0.018f, -0.0015f)
        "ICICI" -> Triple(1090f, 0.015f, 0.003f)
        "ITC" -> Triple(415f, 0.010f, 0.001f)
        else -> Triple(100f, 0.02f, 0.001f)
    }
    val random = java.util.Random(symbol.hashCode().toLong())
    val history = mutableListOf<Float>()
    var current = basePrice
    history.add(current)
    for (i in 1..29) {
        val changePercent = trend + (-volatility + random.nextFloat() * 2f * volatility)
        current *= (1f + changePercent)
        history.add(current)
    }
    return history
}

fun getStockFullName(symbol: String): String {
    return when (symbol) {
        "TCS" -> "Tata Consultancy Services Ltd."
        "BEL" -> "Bharat Electronics Ltd."
        "HDFC" -> "HDFC Bank Ltd."
        "RELIANCE" -> "Reliance Industries Ltd."
        "INFY" -> "Infosys Ltd."
        "ICICI" -> "ICICI Bank Ltd."
        "ITC" -> "ITC Ltd."
        else -> "Selected Equity asset"
    }
}

private fun generatePortfolioCsv(
    holdings: List<Holding>,
    totalVal: Double,
    cost: Double,
    profit: Double,
    pct: Double
): String {
    val sb = StringBuilder()
    sb.append("Portfolio Holdings & Performance Summary\n")
    val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    sb.append("Generated At,${currentDateTime}\n\n")
    
    sb.append("PORTFOLIO PERFORMANCE SUMMARY\n")
    sb.append("Total Valuation,₹${String.format("%.2f", totalVal)}\n")
    sb.append("Total Cost/Invested,₹${String.format("%.2f", cost)}\n")
    sb.append("Net Gain/Loss,₹${String.format("%.2f", profit)}\n")
    sb.append("Return Percentage,${String.format("%.2f", pct)}%\n\n")
    
    sb.append("ACTIVE HOLDINGS DETAILS\n")
    sb.append("Symbol,Company Name,Sector,Quantity,Average Buy Price (₹),Current Price (₹),Total Invested Cost (₹),Current Valuation (₹),Gain/Loss (₹),Gain/Loss (%)\n")
    
    holdings.forEach { holding ->
        val invested = holding.quantity * holding.avgBuyPrice
        val current = holding.quantity * holding.currentPrice
        val holdingProfit = current - invested
        val holdingPct = if (invested > 0) (holdingProfit / invested) * 100 else 0.0
        val escapedName = holding.name.replace("\"", "\"\"")
        
        sb.append("${holding.symbol},\"${escapedName}\",${holding.sector},${holding.quantity},${holding.avgBuyPrice},${holding.currentPrice},${String.format("%.2f", invested)},${String.format("%.2f", current)},${String.format("%.2f", holdingProfit)},${String.format("%.2f", holdingPct)}%\n")
    }
    
    return sb.toString()
}

// ==========================================
// 2. DYNAMIC PORTFOLIO & AUDIT ENGINE
// ==========================================
@Composable
fun PortfolioScreen(viewModel: FinancialViewModel) {
    val context = LocalContext.current
    val holdings by viewModel.holdings.collectAsState()
    val doctorDetails by viewModel.portfolioDoctor.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val watchlistList by viewModel.watchlist.collectAsState()
    val riskAnalysis by viewModel.riskAnalysis.collectAsState()

    var showBuyForm by remember { mutableStateOf(false) }
    var sellSelectedSymbol by remember { mutableStateOf<String?>(null) }

    // State for buy transaction form
    var symbolInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var qtyInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var sectorInput by remember { mutableStateOf("Technology") }

    // State for sell form
    var sellQtyInput by remember { mutableStateOf("") }
    var sellPriceInput by remember { mutableStateOf("") }

    // State for watchlist interest tracker
    var showWatchlistForm by remember { mutableStateOf(false) }
    var watchSymbolInput by remember { mutableStateOf("") }
    var watchNameInput by remember { mutableStateOf("") }
    var watchPriceInput by remember { mutableStateOf("") }
    var watchChangeInput by remember { mutableStateOf("") }

    val totalPortfolioValue = holdings.sumOf { it.quantity * it.currentPrice }
    val totalCost = holdings.sumOf { it.quantity * it.avgBuyPrice }
    val totalProfit = totalPortfolioValue - totalCost
    val profitPercent = if (totalCost > 0) (totalProfit / totalCost) * 100 else 0.0

    val csvExporterLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvText = generatePortfolioCsv(holdings, totalPortfolioValue, totalCost, totalProfit, profitPercent)
                    outputStream.write(csvText.toByteArray())
                    outputStream.flush()
                }
                Toast.makeText(context, "Portfolio exported successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val launchExport = {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        csvExporterLauncher.launch("Sharrow_Portfolio_Summary_$dateStr.csv")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "PORTFOLIO DOCTOR",
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Asset & Allocation Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                ThemeToggle(tag = "action_toggle_theme_portfolio")
            }
        }

        // Portfolio Core summary card
        item {
            GlowCard(borderColor = if (totalProfit >= 0) AccentMint.copy(alpha = 0.4f) else AccentOrange.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PORTFOLIO VALUATION", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%,.2f", totalPortfolioValue)}",
                                color = BrandNavy,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("portfolio_total_valuation")
                            )
                        }

                        OutlinedButton(
                            onClick = { launchExport() },
                            modifier = Modifier.testTag("export_csv_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BrandNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export CSV icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Invested (Cost Basis)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("TOTAL INVESTED", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format("%,.2f", totalCost)}",
                                color = BrandNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("portfolio_total_invested")
                            )
                        }

                        // Net Gain / Loss
                        val sign = if (totalProfit >= 0) "+" else ""
                        val color = if (totalProfit >= 0) AccentMint else AccentOrange
                        val bgAlpha = if (totalProfit >= 0) 0.08f else 0.06f
                        val containerBg = if (totalProfit >= 0) AccentMint.copy(alpha = bgAlpha) else AccentOrange.copy(alpha = bgAlpha)
                        val borderCol = if (totalProfit >= 0) AccentMint.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(containerBg, RoundedCornerShape(12.dp))
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("NET GAIN / LOSS", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (totalProfit >= 0) "▲" else "▼",
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${sign}₹${String.format("%,.2f", totalProfit)}",
                                    color = color,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("portfolio_net_gain_value")
                                )
                            }
                            Text(
                                text = "(${sign}${String.format("%.2f", profitPercent)}%) Since Inception",
                                color = color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("portfolio_net_gain_percent")
                            )
                        }
                    }

                    Divider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Feature 2: Why Did My Portfolio Move Explanation
                    Text("WHY DID PORTFOLIO MOVE?", color = AccentMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val moveText = if (holdings.isEmpty()) {
                        "Your portfolio is empty. Add a stock below to generate an AI movement explanation index!"
                    } else if (totalProfit >= 0) {
                        "Your portfolio gained ₹${String.format("%.0f", totalProfit)} today. Driven primarily by defense order pipeline confirmations bumping BEL by +4.2% and technology consolidation keeping TCS profitable."
                    } else {
                        "Your portfolio corrected today driven by currency headwinds impacting tech consultation revenues."
                    }
                    Text(
                        text = moveText,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.testTag("why_moved_explanation")
                    )
                }
            }
        }

        // Feature: Portfolio Asset Distribution Pie Chart
        item {
            PortfolioPieChartCard(holdings = holdings)
        }

        // Feature 3: Portfolio Doctor Health Audit Score
        item {
            doctorDetails?.let { doctor ->
                GlowCard(borderColor = if (doctor.score >= 80) AccentMint.copy(alpha = 0.4f) else AccentOrange.copy(alpha = 0.4f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, "Doctor", tint = AccentMint)
                                Text(
                                    "AI PORTFOLIO AUDIT",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (doctor.score >= 80) AccentMint.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .border(
                                        1.5.dp,
                                        if (doctor.score >= 80) AccentMint else AccentOrange,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${doctor.score}",
                                    color = if (doctor.score >= 80) AccentMint else AccentOrange,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.testTag("portfolio_health_score")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "VULNERABILITIES DETECTED",
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange,
                            fontSize = 11.sp
                        )
                        doctor.issues.forEach { issue ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("•", color = AccentOrange, fontWeight = FontWeight.Bold)
                                Text(issue, color = TextGray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "RECOMMENDED CORRECTIVE FIXES",
                            fontWeight = FontWeight.Bold,
                            color = AccentMint,
                            fontSize = 11.sp
                        )
                        doctor.recommendations.forEach { recommendation ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("✓", color = AccentMint, fontWeight = FontWeight.Bold)
                                Text(recommendation, color = ContentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Feature 4: AI Risk Analysis Summary Component
        item {
            val riskColor = when (riskAnalysis.riskLevel) {
                "High" -> AccentOrange
                "Medium" -> Color(0xFFC06C00) // Beautiful Amber/Orange for Medium
                else -> AccentMint
            }
            val riskBgColor = when (riskAnalysis.riskLevel) {
                "High" -> AccentOrange.copy(alpha = 0.08f)
                "Medium" -> Color(0xFFC06C00).copy(alpha = 0.08f)
                else -> AccentMint.copy(alpha = 0.08f)
            }
            val riskBadgeText = riskAnalysis.riskLevel.uppercase()

            GlowCard(borderColor = riskColor.copy(alpha = 0.4f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, "Risk", tint = riskColor, modifier = Modifier.size(18.dp))
                            Text(
                                "AI RISK ANALYSIS",
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                fontSize = 13.sp
                            )
                        }

                        // Risk Level Badge
                        Box(
                            modifier = Modifier
                                .background(riskBgColor, RoundedCornerShape(6.dp))
                                .border(1.dp, riskColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$riskBadgeText RISK",
                                color = riskColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Risk Score Progress Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Volatility & Concentration Exposure", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${riskAnalysis.riskScore}/100",
                            color = ContentTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.testTag("risk_analysis_score")
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { riskAnalysis.riskScore.toFloat() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = riskColor,
                        trackColor = BorderColor.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sector concentration details block
                    if (riskAnalysis.maxConcentratedSector != "None") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PEAK SECTOR CONCENTRATION", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(riskAnalysis.maxConcentratedSector, color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CONCENTRATION INDEX", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${String.format("%.1f", riskAnalysis.maxSectorPercentage)}%", color = riskColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // AI Generated Risk Briefing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .background(Color(0xFFFBFDFF), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("QUANTA & SECTOR BRIEFING", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                if (riskAnalysis.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AccentBlue)
                                } else {
                                    IconButton(
                                        onClick = { viewModel.fetchAiRiskBriefing() },
                                        modifier = Modifier.size(18.dp).testTag("action_refresh_risk_analysis")
                                    ) {
                                        Icon(Icons.Default.Refresh, "Refresh", tint = AccentBlue, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = riskAnalysis.aiBriefing,
                                color = ContentTextColor,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.testTag("risk_analysis_ai_briefing")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key vulnerabilities bullet lists
                    Text(
                        "SYSTEMIC VULNERABILITIES DETECTED",
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange,
                        fontSize = 11.sp
                    )
                    riskAnalysis.keyVulnerabilities.forEach { vulnerability ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", color = AccentOrange, fontWeight = FontWeight.Bold)
                            Text(vulnerability, color = TextGray, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Capital reallocation suggestions
                    Text(
                        "PORTFOLIO RELOCATION SUGGESTIONS",
                        fontWeight = FontWeight.Bold,
                        color = AccentMint,
                        fontSize = 11.sp
                    )
                    riskAnalysis.suggestions.forEach { suggestion ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✓", color = AccentMint, fontWeight = FontWeight.Bold)
                            Text(suggestion, color = ContentTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Active Stock Position list header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE ASSET HOLDINGS",
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 1.sp,
                    fontSize = 13.sp
                )
                Button(
                    onClick = { showBuyForm = !showBuyForm },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("action_show_buy_form")
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Record Buy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // BUY TRANSACTION FORM
        if (showBuyForm) {
            item {
                GlowCard(borderColor = AccentMint) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("RECORD ASSET BUY TRANSACTION", fontWeight = FontWeight.Bold, color = BrandNavy, fontSize = 14.sp)

                        OutlinedTextField(
                            value = symbolInput,
                            onValueChange = { symbolInput = it.uppercase() },
                            label = { Text("Ticker Symbol (e.g. BEL)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth().testTag("buy_symbol_input")
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Company Name (e.g. Bharat Electronics)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth().testTag("buy_name_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = qtyInput,
                                onValueChange = { qtyInput = it },
                                label = { Text("Shares", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("buy_qty_input")
                            )
                            OutlinedTextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                label = { Text("Price (INR)", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("buy_price_input")
                            )
                        }

                        // Sector Dropdown list (represented as simple row of choice chips)
                        Text("Company Sector Class", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val sectors = listOf("Technology", "Defense", "Financials", "Energy", "FMCG")
                            sectors.forEach { sector ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sectorInput == sector) AccentMint else BorderColor)
                                        .clickable { sectorInput = sector }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(sector, color = if (sectorInput == sector) Color.White else ContentTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val q = qtyInput.toDoubleOrNull() ?: 0.0
                                val p = priceInput.toDoubleOrNull() ?: 0.0
                                if (symbolInput.isNotEmpty() && q > 0 && p > 0) {
                                    viewModel.recordBuy(symbolInput, nameInput.ifEmpty { symbolInput }, q, p, sectorInput)
                                    showBuyForm = false
                                    symbolInput = ""
                                    nameInput = ""
                                    qtyInput = ""
                                    priceInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                            modifier = Modifier.fillMaxWidth().testTag("buy_submit_button")
                        ) {
                            Text("EXECUTE TRANSACTION", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active holding details list
        if (holdings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, "Empty", tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active portfolio positions found", color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(holdings) { asset ->
                val currentCost = asset.quantity * asset.avgBuyPrice
                val currentVal = asset.quantity * asset.currentPrice
                val profit = currentVal - currentCost

                GlowCard(modifier = Modifier.testTag("holding_${asset.symbol}")) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(asset.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(asset.sector, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Qty: ${asset.quantity} shares", color = TextGray, fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%,.2f", currentVal)}", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                val holdingSign = if (profit >= 0) "+" else ""
                                val profitPercent = if (currentCost > 0) (profit / currentCost) * 100 else 0.0
                                Text(
                                    text = "${holdingSign}₹${String.format("%.1f", profit)} (${holdingSign}${String.format("%.2f", profitPercent)}%)",
                                    color = if (profit >= 0) AccentMint else AccentOrange,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Average Buy Price", color = TextGray, fontSize = 10.sp)
                                Text("₹${String.format("%,.2f", asset.avgBuyPrice)}", color = ContentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("Current Market Price", color = TextGray, fontSize = 10.sp)
                                Text("₹${String.format("%,.2f", asset.currentPrice)}", color = ContentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick = { sellSelectedSymbol = if (sellSelectedSymbol == asset.symbol) null else asset.symbol },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp).testTag("action_sell_${asset.symbol}")
                            ) {
                                Text("Sell", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        // SELL FORM AREA
                        if (sellSelectedSymbol == asset.symbol) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF1F0), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFFDAD6), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("EXECUTE ASSET LIQUIDATION TRADES", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = sellQtyInput,
                                            onValueChange = { sellQtyInput = it },
                                            label = { Text("Shares to sell", color = TextGray, fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                            modifier = Modifier.weight(1f).height(50.dp).testTag("sell_qty_input")
                                        )

                                        OutlinedTextField(
                                            value = sellPriceInput,
                                            onValueChange = { sellPriceInput = it },
                                            label = { Text("Sell Price (INR)", color = TextGray, fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                            modifier = Modifier.weight(1f).height(50.dp).testTag("sell_price_input")
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val sq = sellQtyInput.toDoubleOrNull() ?: 0.0
                                            val sp = sellPriceInput.toDoubleOrNull() ?: 0.0
                                            if (sq > 0 && sp > 0) {
                                                viewModel.recordSell(asset.symbol, sq, sp)
                                                sellSelectedSymbol = null
                                                sellQtyInput = ""
                                                sellPriceInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("sell_submit_button")
                                    ) {
                                        Text("LIQUIDATE AND REGISTER RECORD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Secondary Watchlist tracking list component
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WATCHLIST INTEREST TRACKER",
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 1.sp,
                    fontSize = 13.sp
                )
                Button(
                    onClick = { showWatchlistForm = !showWatchlistForm },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("action_show_watchlist_form_portfolio")
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Add to Watchlist", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (showWatchlistForm) {
            item {
                GlowCard(borderColor = AccentBlue) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("TRACK A STOCK INTEREST", fontWeight = FontWeight.Bold, color = BrandNavy, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = watchSymbolInput,
                            onValueChange = { watchSymbolInput = it.uppercase() },
                            label = { Text("Ticker Symbol (e.g. INFY)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth().testTag("watch_symbol_input")
                        )
                        
                        OutlinedTextField(
                            value = watchNameInput,
                            onValueChange = { watchNameInput = it },
                            label = { Text("Company Name (e.g. Infosys)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth().testTag("watch_name_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = watchPriceInput,
                                onValueChange = { watchPriceInput = it },
                                label = { Text("Track Price (INR)", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("watch_price_input")
                            )
                            OutlinedTextField(
                                value = watchChangeInput,
                                onValueChange = { watchChangeInput = it },
                                label = { Text("Daily Change %", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("watch_change_input")
                            )
                        }

                        Button(
                            onClick = {
                                val price = watchPriceInput.toDoubleOrNull() ?: 1500.0
                                val change = watchChangeInput.toDoubleOrNull() ?: 1.2
                                if (watchSymbolInput.isNotEmpty()) {
                                    viewModel.updateWatchlistAlerts(
                                        watchSymbolInput,
                                        watchNameInput.ifEmpty { watchSymbolInput },
                                        price,
                                        change,
                                        45,
                                        "No target alert set"
                                    )
                                    showWatchlistForm = false
                                    watchSymbolInput = ""
                                    watchNameInput = ""
                                    watchPriceInput = ""
                                    watchChangeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth().testTag("watch_submit_button")
                        ) {
                            Text("ADD TO INTEREST TRACKER", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (watchlistList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No interest watchlist items added yet.", color = TextGray, fontSize = 11.sp)
                }
            }
        } else {
            items(watchlistList) { item ->
                GlowCard(
                    modifier = Modifier.testTag("watchlist_tracker_item_${item.symbol}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(item.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Watchlist", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(item.name, color = TextGray, fontSize = 11.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%,.2f", item.currentPrice)}", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                val changeSign = if (item.changePercent >= 0) "+" else ""
                                val changeColor = if (item.changePercent >= 0) AccentMint else AccentOrange
                                Text(
                                    text = "${changeSign}${String.format("%.2f", item.changePercent)}%",
                                    color = changeColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Divider(color = BorderColor.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Add quickly to portfolio as BUY position
                                OutlinedButton(
                                    onClick = {
                                        symbolInput = item.symbol
                                        nameInput = item.name
                                        priceInput = item.currentPrice.toString()
                                        qtyInput = "10"
                                        showBuyForm = true
                                    },
                                    modifier = Modifier.height(28.dp).testTag("action_watchlist_buy_${item.symbol}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentMint)
                                ) {
                                    Icon(Icons.Default.Add, "Buy", modifier = Modifier.size(12.dp), tint = AccentMint)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Record Buy", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                // Quick details scanner click
                                OutlinedButton(
                                    onClick = {
                                        viewModel.analyzeCustomStock(item.symbol, item.name)
                                    },
                                    modifier = Modifier.height(28.dp).testTag("action_watchlist_scan_${item.symbol}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                                ) {
                                    Icon(Icons.Default.Search, "Scan", modifier = Modifier.size(12.dp), tint = AccentBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Scan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteFromWatchlist(item.symbol) },
                                modifier = Modifier.size(28.dp).testTag("action_watchlist_remove_${item.symbol}")
                            ) {
                                Icon(Icons.Default.Delete, "Remove", tint = AccentOrange.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Chronological Transaction logs header
        item {
            Text(
                "CHRONOLOGICAL AUDIT LEDGER",
                fontWeight = FontWeight.Bold,
                color = BrandNavy,
                letterSpacing = 1.sp,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text("No ledger activities registered on this account.", color = TextGray, fontSize = 11.sp)
            }
        } else {
            items(transactions) { ledger ->
                val simpleDate = remember(ledger.date) {
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(ledger.date))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val actionColor = if (ledger.type == "BUY") AccentMint else AccentOrange
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(actionColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ledger.type.take(1), color = actionColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Column {
                                Text(ledger.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(simpleDate, color = TextGray, fontSize = 10.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val sign = if (ledger.type == "BUY") "-" else "+"
                            Text("${sign}₹${String.format("%,.0f", ledger.shares * ledger.price)}", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${ledger.shares} shares @ ₹${ledger.price}", color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. AI OPPORTUNITY SCANNER & CUSTOM ANALYSER
// ==========================================
@Composable
fun ScannerScreen(viewModel: FinancialViewModel) {
    val bespokeStockResult by viewModel.customStockAnalysis.collectAsState()
    var searchTickerInput by remember { mutableStateOf("") }
    var searchCompanyInput by remember { mutableStateOf("") }

    // Hardcoded daily scans to achieve FEATURE 4
    val staticScannerOpportunities = listOf(
        Triple("BEL", "Top Opportunity (Defense Runway)", "Confidence 90% • Heavy budget tailwinds and massive order backlogs."),
        Triple("TCS", "Hidden Gem (Consolidation Play)", "Confidence 86% • Steady margins despite Western offshore cuts."),
        Triple("RELIANCE", "Momentum Play (Energy Scaling)", "Confidence 82% • Expansion in solar capacity & retail earnings indicators."),
        Triple("ITC", "Value Play (Dividend Ballast)", "Confidence 85% • Robust dividend yields and stable FMCG cash flow.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily market scan alerts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SHARROW.AI OPPORTUNITY SCANNER",
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 1.sp,
                        fontSize = 15.sp
                    )
                    Text("Autonomous daily deep-scans identifying investment highlights.", color = TextGray, fontSize = 12.sp)
                }
                ThemeToggle(tag = "action_toggle_theme_scanner")
            }
        }

        items(staticScannerOpportunities) { (symbol, badge, description) ->
            GlowCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(symbol, color = AccentMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(badge, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = {
                                searchTickerInput = symbol
                                viewModel.analyzeCustomStock(symbol, symbol)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Search, "Analyze", tint = AccentMint)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(description, color = TextGray, fontSize = 12.sp)
                }
            }
        }

        // Feature 9 + Custom lookup: Dynamic Active Stock Conviction Analyser
        item {
            GlowCard(borderColor = AccentBlue.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, "Radar Scope", tint = AccentBlue)
                        Text(
                            "ACTIVE AI CONVICTION ANALYSER",
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        "Submit any company ticker code (e.g. RELIANCE, BEL, AAPL, MSFT) below to run a direct live-prompted evaluation.",
                        color = TextGray, fontSize = 12.sp, lineHeight = 16.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchTickerInput,
                            onValueChange = { searchTickerInput = it.uppercase() },
                            label = { Text("Ticker (e.g. RELIANCE)", color = TextGray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.weight(1.2f).testTag("search_ticker_input")
                        )
                        OutlinedTextField(
                            value = searchCompanyInput,
                            onValueChange = { searchCompanyInput = it },
                            label = { Text("Company (Optional)", color = TextGray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.weight(1.8f).testTag("search_company_input")
                        )
                    }

                    Button(
                        onClick = {
                            if (searchTickerInput.isNotEmpty()) {
                                viewModel.analyzeCustomStock(searchTickerInput, searchCompanyInput.ifEmpty { searchTickerInput })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth().testTag("search_submit_button")
                    ) {
                        Text("RUN LIVE DEEP ANALYSIS", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // ANALYSIS OUTCOME REPORT SCREEN
                    bespokeStockResult?.let { result ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            if (result.isLoading) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = AccentBlue)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Neural analyser processing market filings...", color = TextGray, fontSize = 12.sp)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CanvasBg)
                                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                        .padding(14.dp)
                                        .testTag("ai_conviction_outcome"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "ANALYSIS FOR: ${result.symbol}",
                                            fontWeight = FontWeight.Bold,
                                            color = BrandNavy,
                                            fontSize = 14.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(AccentMint.copy(alpha = 0.15f), CircleShape)
                                                .border(1.dp, AccentMint, CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Convicting Score: ${result.score}",
                                                color = AccentMint,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("AI Grading:", color = TextGray, fontSize = 12.sp)
                                        Text(result.rating, color = AccentMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Primary Analyst Verdict:", color = ContentTextColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    Text(result.reasoning, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp)

                                    Divider(color = BorderColor)

                                    Text("CATALYST PULSES Detected:", color = AccentMint, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    result.catalysts.forEach { cat ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("✦", color = AccentMint)
                                            Text(cat, color = TextGray, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("CRITICAL SECULAR RISKS Detected:", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    result.risks.forEach { r ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("✦", color = AccentOrange)
                                            Text(r, color = TextGray, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Expected Technical Outlook: ${result.outlook}", color = ContentTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. ACTIVE ALERTS & AI NEWS INTELLIGENCE
// ==========================================
@Composable
fun WatchlistScreen(viewModel: FinancialViewModel) {
    val watchlistList by viewModel.watchlist.collectAsState()
    val newsList by viewModel.newsArticles.collectAsState()

    var showAddWatchlistForm by remember { mutableStateOf(false) }
    var symbolWInput by remember { mutableStateOf("") }
    var nameWInput by remember { mutableStateOf("") }
    var customRsi by remember { mutableStateOf("45") }
    var customMacd by remember { mutableStateOf("Neutral signal") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature 6: Active monitoring setup
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI WATCHLIST ALERTS",
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 1.sp,
                        fontSize = 15.sp
                    )
                    Text("Trigger alarms for RSI boundaries & MACD surges.", color = TextGray, fontSize = 12.sp)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeToggle(tag = "action_toggle_theme_watchlist")
                    IconButton(
                        onClick = { showAddWatchlistForm = !showAddWatchlistForm },
                        modifier = Modifier.testTag("action_show_watchlist_form")
                    ) {
                        Icon(Icons.Default.Add, "Add Alert", tint = AccentMint, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Add to watchlist alert form
        if (showAddWatchlistForm) {
            item {
                GlowCard(borderColor = AccentMint) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("CONFIGURE DYNAMIC ALARM TARGET", fontWeight = FontWeight.Bold, color = BrandNavy, fontSize = 14.sp)

                        OutlinedTextField(
                            value = symbolWInput,
                            onValueChange = { symbolWInput = it.uppercase() },
                            label = { Text("Ticker Symbol", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth().testTag("watchlist_symbol_input")
                        )

                        OutlinedTextField(
                            value = nameWInput,
                            onValueChange = { nameWInput = it },
                            label = { Text("Company Name", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = customRsi,
                                onValueChange = { customRsi = it },
                                label = { Text("Alarm RSI limit", color = TextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("watchlist_rsi_input")
                            )
                            OutlinedTextField(
                                value = customMacd,
                                onValueChange = { customMacd = it },
                                label = { Text("MACD Trigger Notes", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1f).testTag("watchlist_macd_input")
                            )
                        }

                        Button(
                            onClick = {
                                if (symbolWInput.isNotEmpty()) {
                                    viewModel.updateWatchlistAlerts(
                                        symbolWInput,
                                        nameWInput.ifEmpty { symbolWInput },
                                        350.0,
                                        0.0,
                                        customRsi.toIntOrNull() ?: 45,
                                        customMacd
                                    )
                                    showAddWatchlistForm = false
                                    symbolWInput = ""
                                    nameWInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                            modifier = Modifier.fillMaxWidth().testTag("watchlist_submit_button")
                        ) {
                            Text("SAVE SYSTEM MONITORING ALERTS", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Alerts rendered
        items(watchlistList) { item ->
            GlowCard(
                borderColor = if (item.rsi < 30) AccentMint.copy(alpha = 0.5f) else BorderColor,
                modifier = Modifier.testTag("watchlist_item_${item.symbol}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(item.name, color = TextGray, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = { viewModel.deleteFromWatchlist(item.symbol) },
                            modifier = Modifier.testTag("action_delete_alert_${item.symbol}")
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = AccentOrange.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("RSI Value Gauge", color = TextGray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${item.rsi}", color = if (item.rsi < 30) AccentMint else ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (item.rsi < 30) {
                                    Box(
                                        modifier = Modifier
                                            .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("Oversold (Buy Reverse)", color = AccentMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("MACD Trigger Status", color = TextGray, fontSize = 10.sp)
                            Text(item.macd, color = ContentTextColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Feature 5: News Intelligence Center
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "AI NEWS INTELLIGENCE FEED",
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 1.sp,
                    fontSize = 15.sp
                )
                Text("Tap 'Analyze article' to compute bullet summaries, risks, and sentiment logs.", color = TextGray, fontSize = 11.sp)
            }
        }

        items(newsList) { story ->
            GlowCard(modifier = Modifier.testTag("news_${story.id}")) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(story.source, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Button(
                            onClick = { viewModel.analyzeNewsArticle(story.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(24.dp).testTag("action_analyze_news_${story.id}")
                        ) {
                            if (story.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(10.dp))
                            } else {
                                Text("Analyze news", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(story.title, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(story.baseSummary, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp)

                    if (story.aiSummary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CanvasBg)
                                .border(1.dp, AccentMint.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, "Feedback", tint = AccentMint, modifier = Modifier.size(15.dp))
                                    Text("SHARROW.AI FEEDBACK SUMMARY", color = AccentMint, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Text(story.aiSummary, color = ContentTextColor, fontSize = 11.sp, lineHeight = 15.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Bullish: ${story.bullishScore}%", color = AccentMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Bearish: ${story.bearishScore}%", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Relevance: ${story.relevanceToUser}", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. GLOBAL SUBSCRIPTION & ADMIN ANALYTICS
// ==========================================
@Composable
fun SubscriptionScreen(viewModel: FinancialViewModel) {
    val subState by viewModel.subscriptionState.collectAsState()

    var showAdminDashboard by remember { mutableStateOf(false) }
    var couponCodeInput by remember { mutableStateOf("") }
    var couponFeedback by remember { mutableStateOf<String?>(null) }
    var discountMultiplier by remember { mutableFloatStateOf(1f) }

    // Dynamic country rates (Feature 12 + 13 - Outcome Selling Selling Outcomes)
    val countries = listOf("India", "USA", "UK")
    val selectedCountry = subState?.country ?: "India"

    val baseProRate = when (selectedCountry) {
        "USA" -> 9.99
        "UK" -> 8.99
        else -> 499.0
    }
    val baseEliteRate = when (selectedCountry) {
        "USA" -> 19.99
        "UK" -> 17.99
        else -> 999.0
    }
    val symbolPr = when (selectedCountry) {
        "USA" -> "$"
        "UK" -> "£"
        else -> "₹"
    }

    val proRateDisplay = baseProRate * discountMultiplier
    val eliteRateDisplay = baseEliteRate * discountMultiplier

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subscription Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "PREMIUM PLANS",
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Manage Active Subscription",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                ThemeToggle(tag = "action_toggle_theme_sub")
            }
        }

        // Subscription State header card
        item {
            GlowCard(borderColor = AccentMint) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE MEMBERSHIP DELEGATE", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = (subState?.planType ?: "Free").uppercase() + " PACKAGE ACTIVE",
                        color = BrandNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("subscription_status_display")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Billing Zone: ${subState?.country ?: "India"} • Local Token Cost: ${subState?.currency ?: "₹"}${subState?.priceText ?: "Free"}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    if (subState?.couponApplied != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Discount Applied: ${subState?.couponApplied}", color = AccentMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Toggles zones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Regional Zone", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    countries.forEach { zone ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCountry == zone) AccentBlue else BorderColor)
                                .clickable {
                                    val newSym = when (zone) {
                                        "USA" -> "$"
                                        "UK" -> "£"
                                        else -> "₹"
                                    }
                                    viewModel.executePremiumUpgrade(subState?.planType ?: "Free", zone, newSym, subState?.priceText ?: "Free", subState?.couponApplied)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(zone, color = if (selectedCountry == zone) Color.White else ContentTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Coupon input box
        item {
            GlowCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("REDUCE FEES - ENTER ALIGNED PROMO CODE", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCodeInput,
                            onValueChange = { couponCodeInput = it },
                            placeholder = { Text("Enter SHARROW50", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                            modifier = Modifier.weight(1.8f).height(50.dp).testTag("coupon_input")
                        )
                        Button(
                            onClick = {
                                if (couponCodeInput.uppercase() == "SHARROW50") {
                                    discountMultiplier = 0.5f
                                    couponFeedback = "Promo MATCH! 50% discount registered."
                                } else {
                                    couponFeedback = "Invalid coupon code."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                            modifier = Modifier.weight(1.2f).height(42.dp).testTag("coupon_submit_button")
                        ) {
                            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    couponFeedback?.let {
                        Text(it, color = if (it.contains("MATCH")) AccentMint else AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub plans (Featured 13: sells outcomes)
        item {
            Text("CHOOSE YOUR OUTCOME PLAN", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Package Pro
        item {
            GlowCard(borderColor = if (subState?.planType == "Pro") AccentBlue else BorderColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PRO PLAN", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Professional Portfolio Audit", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$symbolPr${String.format("%.2f", proRateDisplay)}/mo",
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("✓ Custom algorithmic alerts with oversold diagnostic logs", color = TextGray, fontSize = 12.sp)
                    Text("✓ Automatic Sector Rotation flow updates", color = TextGray, fontSize = 12.sp)
                    Text("✓ Local persistent holding tracking dashboard", color = TextGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.executePremiumUpgrade(
                                "Pro",
                                selectedCountry,
                                symbolPr,
                                "${String.format("%.2f", proRateDisplay)}/month",
                                if (discountMultiplier < 1f) "SHARROW50" else null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_pro_button")
                    ) {
                        Text("Upgrade to Active Audit License", color = Color.White)
                    }
                }
            }
        }

        // Package Elite
        item {
            GlowCard(borderColor = if (subState?.planType == "Elite") AccentMint else BorderColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ELITE PLAN", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("AI Opportunity Detection Engine", color = AccentMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$symbolPr${String.format("%.2f", eliteRateDisplay)}/mo",
                            color = AccentMint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("✓ Unlimited AI Market briefings via live Gemini REST", color = TextGray, fontSize = 12.sp)
                    Text("✓ Fully dynamic Custom Ticker Analyst Conviction report scanner", color = TextGray, fontSize = 12.sp)
                    Text("✓ Single-click AI News summarization & sentiment scoring module", color = TextGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.executePremiumUpgrade(
                                "Elite",
                                selectedCountry,
                                symbolPr,
                                "${String.format("%.2f", eliteRateDisplay)}/month",
                                if (discountMultiplier < 1f) "SHARROW50" else null
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_elite_button")
                    ) {
                        Text("Acquire Total Intelligence License", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 15: Admin Intelligence Dashboard Trigger
        item {
            Divider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
            Button(
                onClick = { showAdminDashboard = !showAdminDashboard },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth().testTag("admin_dashboard_toggle")
            ) {
                Icon(Icons.Default.Settings, "Admin", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("TOGGLE TELEMETRY ADMIN MODULE", color = Color.White)
            }
        }

        // ADMIN INTELLIGENCE HOVER DISPLAY (All indices from FEATURE 15)
        if (showAdminDashboard) {
            item {
                GlowCard(borderColor = AccentOrange.copy(alpha = 0.5f)) {
                    Column(
                        modifier = Modifier.padding(16.dp).testTag("admin_intel_dashboard"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, "Metrics", tint = AccentOrange)
                            Text(
                                "ADMIN REVENUE TELEMETRY",
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                fontSize = 13.sp
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MRR Forecast", color = TextGray, fontSize = 10.sp)
                                Text("$124,560", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ARR Forecast", color = TextGray, fontSize = 10.sp)
                                Text("$1,494,720", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Subscribers", color = TextGray, fontSize = 10.sp)
                                Text("12,450 active", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trial Cov Rate", color = TextGray, fontSize = 10.sp)
                                Text("18.42%", color = AccentMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Churn Rate", color = TextGray, fontSize = 10.sp)
                                Text("2.10%", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CAC index", color = TextGray, fontSize = 10.sp)
                                Text("$3.45", color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        Divider(color = BorderColor)
                        Text("Regional Growth Indexes:", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("• India (Zone-1): 58% gross conversion momentum", color = TextGray, fontSize = 11.sp)
                        Text("• USA (Zone-2): $42,400 MRR generated quarterly", color = TextGray, fontSize = 11.sp)
                        Text("• United Kingdom: Heavy Elite licensing pickup", color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
