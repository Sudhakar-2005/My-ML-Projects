package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle

// --- High Density Theme Palette (Dynamic M3) ---
var isDarkThemeState by mutableStateOf(true)

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
    val isDark = isDarkThemeState
    
    // Glassmorphic border brush: higher opacity top-left, lower opacity bottom-right
    val borderBrush = remember(isDark, borderColor) {
        val topColor = borderColor?.copy(alpha = 0.55f) ?: (if (isDark) Color(0x3DFFFFFF) else Color(0x24000000))
        val bottomColor = borderColor?.copy(alpha = 0.15f) ?: (if (isDark) Color(0x0FFFFFFF) else Color(0x08000000))
        Brush.linearGradient(
            colors = listOf(topColor, bottomColor),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        )
    }

    // Glassmorphic background gradient: translucent surface with subtle highlights
    val backgroundBrush = remember(isDark) {
        val startColor = if (isDark) Color(0x9C1C1E24) else Color(0xE6FFFFFF)
        val endColor = if (isDark) Color(0x73111317) else Color(0xB3F1F5F9)
        Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderBrush, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
        ) {
            content()
        }
    }
}

@Composable
fun FramerMotionEntrance(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 500,
    yOffset: Float = 30f, // slide up starting offset in Dp equivalent
    initialScale: Float = 0.96f, // initial scale down
    content: @Composable () -> Unit
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            kotlinx.coroutines.delay(delayMillis.toLong())
        }
        animStarted = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "framer_motion_alpha"
    )

    val translationY by animateFloatAsState(
        targetValue = if (animStarted) 0f else yOffset,
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "framer_motion_translationY"
    )

    val scale by animateFloatAsState(
        targetValue = if (animStarted) 1f else initialScale,
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "framer_motion_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(
                alpha = alpha,
                translationY = translationY * androidx.compose.ui.platform.LocalDensity.current.density,
                scaleX = scale,
                scaleY = scale
            )
    ) {
        content()
    }
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
            FramerMotionEntrance(delayMillis = 0, yOffset = 24f) {
                if (isRefreshing || alphaReport == null) {
                    AlphaReportSkeleton(modifier = Modifier.testTag("ai_market_radar_card"))
                } else {
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
            }
        }

        // Feature: Market DNA Dashboard Sentiment Gauge
        item {
            FramerMotionEntrance(delayMillis = 75, yOffset = 24f) {
                MarketDnaDashboardCard(viewModel = viewModel)
            }
        }

        // Stock Performance History Chart (30 Days)
        item {
            FramerMotionEntrance(delayMillis = 150, yOffset = 24f) {
                StockPerformanceLineChartCard()
            }
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

            FramerMotionEntrance(delayMillis = 300, yOffset = 24f) {
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

        itemsIndexed(sectorRotations) { index, strength ->
            FramerMotionEntrance(delayMillis = 400 + index * 100, yOffset = 20f) {
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
    }

        // Feature 9: Money Flow Visualizer
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FramerMotionEntrance(delayMillis = 650, yOffset = 20f) {
                MoneyFlowVisualizerCard(viewModel)
            }
        }

        // Feature: AI Debate Engine
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FramerMotionEntrance(delayMillis = 680, yOffset = 20f) {
                AIDebateEngineCard(viewModel)
            }
        }

        // Feature: World Market Pulse
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FramerMotionEntrance(delayMillis = 690, yOffset = 20f) {
                WorldMarketPulseCard(viewModel)
            }
        }

        // Feature: AI Research Report Generator
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FramerMotionEntrance(delayMillis = 695, yOffset = 20f) {
                AIResearchReportGeneratorCard(viewModel)
            }
        }

        // Feature: Investment Timeline Replay
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FramerMotionEntrance(delayMillis = 698, yOffset = 20f) {
                InvestmentTimelineReplayCard(viewModel)
            }
        }

        // Feature 10: AI Mentor Educational Coach
        item {
            FramerMotionEntrance(delayMillis = 700, yOffset = 20f) {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📊", fontSize = 28.sp)
                        Text(
                            text = "Add holdings to unlock AI insights.",
                            color = BrandNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Once you add shares to your portfolio, dynamic asset weight distributions and hedge models will auto-generate here.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
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
    var selectedTimeframe by remember { mutableStateOf(30) } // 7, 30, or 90
    
    var isLocalLoading by remember(selectedSymbol, selectedTimeframe) { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(selectedSymbol, selectedTimeframe) {
        isLocalLoading = true
        kotlinx.coroutines.delay(400)
        isLocalLoading = false
    }

    if (isLocalLoading) {
        StockPerformanceLineChartSkeleton(modifier = modifier.testTag("stock_performance_chart_card"))
    } else {
        val history = remember(selectedSymbol, selectedTimeframe) { getStockHistory(selectedSymbol, selectedTimeframe) }
        var activeIndex by remember(selectedSymbol, selectedTimeframe) { mutableStateOf<Int?>(null) }
        
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
                        "$selectedTimeframe-DAY PERFORMANCE GRAPH (RECHARTS STYLE)",
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
            
            Spacer(modifier = Modifier.height(10.dp))

            // Timeframe Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timeframe:",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                listOf(7, 30, 90).forEach { timeframe ->
                    val isSelected = selectedTimeframe == timeframe
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeframe = timeframe },
                        label = { Text("${timeframe}D", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                        modifier = Modifier.testTag("chip_timeframe_$timeframe")
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
                        text = if (activeIndex != null) "GAIN FROM DAY 1" else "$selectedTimeframe-DAY TOTAL RETURN",
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
                        val maxIdx = (history.size - 1).coerceAtLeast(1)
                        val maxIdxFloat = maxIdx.toFloat()

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(history) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val width = size.width
                                            val idx = ((offset.x / width) * maxIdxFloat).roundToInt().coerceIn(0, maxIdx)
                                            activeIndex = idx
                                        },
                                        onDrag = { change, _ ->
                                            val width = size.width
                                            val idx = ((change.position.x / width) * maxIdxFloat).roundToInt().coerceIn(0, maxIdx)
                                            activeIndex = idx
                                        },
                                        onDragEnd = { /* keep active select to inspect */ },
                                        onDragCancel = { }
                                    )
                                }
                                .pointerInput(history) {
                                    detectTapGestures(
                                        onPress = { offset ->
                                            val width = size.width
                                            val idx = ((offset.x / width) * maxIdxFloat).roundToInt().coerceIn(0, maxIdx)
                                            activeIndex = idx
                                        }
                                    )
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            
                            val heightOffset = 12.dp.toPx()
                            val usableHeight = height - heightOffset * 2f
                            val spacingX = width / maxIdxFloat
                            
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
                                val tooltipOnLeft = index > history.size / 2
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
                val xLabels = when (selectedTimeframe) {
                    7 -> listOf("Day 1", "Day 3", "Day 5", "Day 7")
                    90 -> listOf("Day 1", "Day 30", "Day 60", "Day 90")
                    else -> listOf("Day 1", "Day 10", "Day 20", "Day 30")
                }
                xLabels.forEach { dayLabel ->
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
}

fun getStockHistory(symbol: String, days: Int): List<Float> {
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
    for (i in 1 until days) {
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

@Composable
fun RechartsDoughnutSummaryWidget(
    holdings: List<Holding>,
    onStockSelected: ((String) -> Unit)? = null
) {
    val simulatedDailyReturns = mapOf(
        "TCS" to 1.85,
        "BEL" to 4.20,
        "INFY" to -0.95,
        "SBI" to 2.10,
        "TATASTEEL" to 1.35,
        "RELIANCE" to 0.75,
        "HDFCBANK" to -0.45,
        "ICICIBANK" to 1.15
    )

    val totalPortfolioValue = holdings.sumOf { it.quantity * it.currentPrice }
    
    // Calculate simulated day's gain/loss based on individual asset performances
    val totalDayProfit = holdings.sumOf { holding ->
        val currentVal = holding.quantity * holding.currentPrice
        val dailyReturnPercent = simulatedDailyReturns[holding.symbol] ?: 0.50
        currentVal * (dailyReturnPercent / 100.0)
    }
    
    val dayProfitPercent = if (totalPortfolioValue > 0) {
        (totalDayProfit / totalPortfolioValue) * 100.0
    } else {
        0.0
    }

    val rechartsColors = listOf(
        Color(0xFF0EA5E9), // Sky Blue (Primary)
        Color(0xFF10B981), // Emerald (Success)
        Color(0xFF6366F1), // Indigo
        Color(0xFFF59E0B), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFF8B5CF6), // Purple
        Color(0xFF14B8A6), // Teal
        Color(0xFFF43F5E), // Rose
        Color(0xFF84CC16)  // Lime
    )

    val transitionState = remember { Animatable(0f) }
    LaunchedEffect(holdings) {
        transitionState.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    var selectedIndex by remember { mutableStateOf(-1) }

    GlowCard(borderColor = AccentBlue.copy(alpha = 0.3f)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Widget Header mimicking Recharts clean dashboard panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), CircleShape) // Live green pulse
                        )
                        Text(
                            text = "RECHARTS ENGINE ACTIVE",
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Portfolio Summary Dashboard",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }

                // Small badge showing tech stack
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Doughnut v2.8",
                        color = TextGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main stats section (Total Balance & Day's Gain/Loss)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "TOTAL BALANCE",
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${String.format("%,.2f", totalPortfolioValue)}",
                        color = BrandNavy,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("portfolio_recharts_balance")
                    )
                }

                // Day's Change Card
                val profitSign = if (totalDayProfit >= 0) "+" else ""
                val profitColor = if (totalDayProfit >= 0) AccentMint else AccentOrange
                val profitBg = if (totalDayProfit >= 0) AccentMint.copy(alpha = 0.08f) else AccentOrange.copy(alpha = 0.08f)
                val profitBorder = if (totalDayProfit >= 0) AccentMint.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f)

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .background(profitBg, RoundedCornerShape(8.dp))
                        .border(1.dp, profitBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "DAY'S P&L",
                        color = TextGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (totalDayProfit >= 0) "▲" else "▼",
                            color = profitColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profitSign}₹${String.format("%,.2f", Math.abs(totalDayProfit))}",
                            color = profitColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("portfolio_recharts_day_profit")
                        )
                    }
                    Text(
                        text = "${profitSign}${String.format("%.2f", dayProfitPercent)}%",
                        color = profitColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("portfolio_recharts_day_percent")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Doughnut Chart Section
            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🍩", fontSize = 28.sp)
                        Text(
                            text = "Add holdings to unlock AI insights.",
                            color = BrandNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add assets in the holdings manager to build the neural Recharts breakdown models dynamically.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                val slices = holdings.mapIndexed { index, holding ->
                    val value = holding.quantity * holding.currentPrice
                    val percentage = if (totalPortfolioValue > 0) (value / totalPortfolioValue) * 100.0 else 0.0
                    val color = rechartsColors[index % rechartsColors.size]
                    ChartSlice(holding.symbol, value, percentage, color)
                }.sortedByDescending { it.value }

                // Doughnut Render and Interaction Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .testTag("recharts_doughnut_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        var centerPoint by remember { mutableStateOf(Offset.Zero) }
                        val density = LocalDensity.current
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(slices) {
                                    detectTapGestures { tapOffset ->
                                        val dx = tapOffset.x - centerPoint.x
                                        val dy = tapOffset.y - centerPoint.y
                                        val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                                        
                                        val outerRadius = size.width.toFloat() / 2f
                                        val innerRadius = outerRadius - (32f * density.density)
                                        
                                        if (distance in innerRadius..outerRadius) {
                                            var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                            var normalizedAngle = angle + 90f
                                            if (normalizedAngle < 0) {
                                                normalizedAngle += 360f
                                            }
                                            
                                            var accumAngle = 0f
                                            var clickedIndex = -1
                                            slices.forEachIndexed { idx, slice ->
                                                val sweep = (slice.percentage.toFloat() / 100f) * 360f
                                                if (normalizedAngle >= accumAngle && normalizedAngle < accumAngle + sweep) {
                                                    clickedIndex = idx
                                                }
                                                accumAngle += sweep
                                            }
                                            selectedIndex = if (selectedIndex == clickedIndex) -1 else clickedIndex
                                            if (selectedIndex != -1) {
                                                onStockSelected?.invoke(slices[selectedIndex].symbol)
                                            }
                                        } else {
                                            selectedIndex = -1
                                        }
                                    }
                                }
                        ) {
                            centerPoint = Offset(size.width / 2f, size.height / 2f)
                            var startAngle = -90f
                            val strokeWidth = 24.dp.toPx()
                            
                            slices.forEachIndexed { index, slice ->
                                val sweepAngle = (slice.percentage.toFloat() / 100f) * 360f * transitionState.value
                                val isSelected = selectedIndex == index
                                val drawStrokeWidth = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth
                                val drawColor = if (selectedIndex == -1 || isSelected) slice.color else slice.color.copy(alpha = 0.35f)
                                
                                if (sweepAngle > 0) {
                                    drawArc(
                                        color = drawColor,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = drawStrokeWidth, cap = StrokeCap.Butt)
                                    )
                                }
                                startAngle += sweepAngle
                            }
                        }

                        // Center content of Doughnut
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedIndex == -1) {
                                Text(
                                    "PORTFOLIO",
                                    color = TextGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "₹${String.format("%,.0f", totalPortfolioValue)}",
                                    color = BrandNavy,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text("Tap slice", color = AccentBlue, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                            } else {
                                val selectedSlice = slices[selectedIndex]
                                Text(
                                    text = selectedSlice.symbol,
                                    color = selectedSlice.color,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${String.format("%.1f", selectedSlice.percentage)}%",
                                    color = BrandNavy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${String.format("%,.0f", selectedSlice.value)}",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recharts-inspired Custom Hover Tooltip Panel
                AnimatedVisibility(
                    visible = selectedIndex != -1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (selectedIndex in slices.indices) {
                        val slice = slices[selectedIndex]
                        val companyName = getStockFullName(slice.symbol)
                        val sliceReturn = simulatedDailyReturns[slice.symbol] ?: 0.50
                        val sliceReturnSign = if (sliceReturn >= 0) "+" else ""
                        val returnColor = if (sliceReturn >= 0) AccentMint else AccentOrange
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp)) // Dark Slate theme matching Recharts web tooltip
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(slice.color, CircleShape)
                                        )
                                        Text(
                                            text = companyName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = slice.symbol,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color(0xFF1E293B), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("RECHARTS VALUATION", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("₹${String.format("%,.2f", slice.value)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("WEIGHT", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("${String.format("%.2f", slice.percentage)}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("DAY CHANGE", color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text("${sliceReturnSign}${sliceReturn}%", color = returnColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Recharts Legend Section
                Text(
                    "RECHARTS ASSET WEIGHTS LEGEND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Render legend items in clean structured grid/list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEachIndexed { idx, slice ->
                        val isSelected = selectedIndex == idx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) slice.color.copy(alpha = 0.08f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) slice.color.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedIndex = if (selectedIndex == idx) -1 else idx
                                    if (selectedIndex != -1) {
                                        onStockSelected?.invoke(slice.symbol)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(slice.color, CircleShape)
                                )
                                Text(
                                    text = slice.symbol,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = getStockFullName(slice.symbol),
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "₹${String.format("%,.0f", slice.value)}",
                                    color = BrandNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(slice.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", slice.percentage)}%",
                                        color = slice.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
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

        // Portfolio Recharts Doughnut Summary Widget
        item {
            FramerMotionEntrance(delayMillis = 0, yOffset = 24f) {
                RechartsDoughnutSummaryWidget(
                    holdings = holdings,
                    onStockSelected = { symbol ->
                        viewModel.selectStockForSummary(symbol, getStockFullName(symbol))
                    }
                )
            }
        }

        // Feature 3: Portfolio Doctor Health Audit Score
        item {
            FramerMotionEntrance(delayMillis = 200, yOffset = 24f) {
                if (doctorDetails == null) {
                    PortfolioDoctorSkeleton()
                } else {
                    val doctor = doctorDetails!!
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

            FramerMotionEntrance(delayMillis = 300, yOffset = 24f) {
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
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💼", fontSize = 36.sp)
                        Text(
                            text = "Build your first AI-powered portfolio.",
                            color = BrandNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add custom shares above to sync real-time market valuations & neural auditing.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            itemsIndexed(holdings) { index, asset ->
                val currentCost = asset.quantity * asset.avgBuyPrice
                val currentVal = asset.quantity * asset.currentPrice
                val profit = currentVal - currentCost

                FramerMotionEntrance(delayMillis = 400 + index * 100, yOffset = 20f) {
                    GlowCard(modifier = Modifier.testTag("holding_${asset.symbol}")) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.selectStockForSummary(asset.symbol, getStockFullName(asset.symbol))
                                        }
                                        .testTag("ai_trigger_${asset.symbol}")
                                ) {
                                    Text(asset.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(asset.sector, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "AI Summary",
                                        tint = AccentBlue.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
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
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔔", fontSize = 36.sp)
                        Text(
                            text = "Start tracking companies you care about.",
                            color = BrandNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add custom stocks above to trigger automated smart alerts, target watch models, and active monitoring.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
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
                                viewModel.selectStockForSummary(symbol, getStockFullName(symbol))
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
    val isRefreshingNews by viewModel.isRefreshingNews.collectAsState()

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
        if (watchlistList.isEmpty()) {
            item {
                WatchlistSkeleton()
            }
        } else {
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
                        Column(
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectStockForSummary(item.symbol, item.name)
                                }
                                .testTag("ai_trigger_watchlist_${item.symbol}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.symbol, color = ContentTextColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "AI Summary",
                                    tint = AccentBlue.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
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
        }

        // Feature 5: News Intelligence Center
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI NEWS INTELLIGENCE FEED",
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        letterSpacing = 1.sp,
                        fontSize = 15.sp
                    )
                    Text("Powered by live Google Search grounding of your portfolio ticker news.", color = TextGray, fontSize = 11.sp)
                }
                
                IconButton(
                    onClick = { viewModel.refreshNewsFromGoogleSearch() },
                    modifier = Modifier.testTag("action_refresh_google_news")
                ) {
                    if (isRefreshingNews) {
                        CircularProgressIndicator(
                            color = AccentBlue,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh news from Google Search",
                            tint = AccentBlue
                        )
                    }
                }
            }
        }

        if (isRefreshingNews) {
            items(3) {
                NewsArticleItemSkeleton()
            }
        } else if (newsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📰", fontSize = 36.sp)
                        Text(
                            text = "We're gathering the latest intelligence.",
                            color = BrandNavy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Hit the refresh button above to query live Google Search grounding of your portfolio ticker news.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
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
}


// ==========================================
// 5. GLOBAL SUBSCRIPTION & ADMIN ANALYTICS
// ==========================================
@Composable
fun SubscriptionScreen(viewModel: FinancialViewModel) {
    val subState by viewModel.subscriptionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Plans & Checkout, 1 = Security & Auth 2.0
    var showAdminDashboard by remember { mutableStateOf(false) }
    var couponCodeInput by remember { mutableStateOf("") }
    var couponFeedback by remember { mutableStateOf<String?>(null) }
    var discountMultiplier by remember { mutableFloatStateOf(1f) }

    // Checkout Sim States
    var selectedCheckoutPlan by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var checkoutProcessing by remember { mutableStateOf(false) }
    var checkoutCompleted by remember { mutableStateOf(false) }
    var checkoutReceiptId by remember { mutableStateOf("") }

    // Security States
    var biometricEnabled by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var sessionHistoryList by remember { mutableStateOf(listOf(
        Triple("Google Pixel 8 Pro", "Mumbai, India • Active Now", "Android 14"),
        Triple("Chrome on MacOS", "Bengaluru, India • 2 hours ago", "Mac OS X"),
        Triple("iPhone 15 Pro Max", "Delhi, India • 3 days ago", "iOS 17")
    )) }

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
    val baseFamilyRate = when (selectedCountry) {
        "USA" -> 29.99
        "UK" -> 25.99
        else -> 1499.0
    }
    val symbolPr = when (selectedCountry) {
        "USA" -> "$"
        "UK" -> "£"
        else -> "₹"
    }

    val proRateDisplay = baseProRate * discountMultiplier
    val eliteRateDisplay = baseEliteRate * discountMultiplier
    val familyRateDisplay = baseFamilyRate * discountMultiplier

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SHARROW EXECUTIVE PANEL",
                        fontWeight = FontWeight.Bold,
                        color = TextGray,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Billing, Plans & Identity 2.0",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                ThemeToggle(tag = "action_toggle_theme_sub")
            }
        }

        // Sub Tabs switcher: 0 = Plans & Gateway, 1 = Identity & Biometrics
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BorderColor.copy(alpha = 0.2f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == 0) AccentBlue else Color.Transparent)
                        .clickable { activeSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Plans & Payment Gateway",
                        color = if (activeSubTab == 0) Color.White else ContentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeSubTab == 1) AccentBlue else Color.Transparent)
                        .clickable { activeSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Security & Auth 2.0",
                        color = if (activeSubTab == 1) Color.White else ContentTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (activeSubTab == 0) {
            // ==================== TAB 1: PLANS & GATEWAY ====================

            // Current License Status Card
            item {
                GlowCard(borderColor = AccentMint) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LICENSE ENTITLEMENT", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SECURE STATUS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentMint)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = (subState?.planType ?: "Free").uppercase() + " INTELLIGENCE PASS",
                            color = BrandNavy,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("subscription_status_display")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Billing Anchor: ${subState?.country ?: "India"} • Active Cost: ${subState?.currency ?: "₹"}${subState?.priceText ?: "Free"}",
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
                                Text("Active Coupon: ${subState?.couponApplied}", color = AccentMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Usage & Quota Meter Dashboard
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("PLATFORM NEURAL USAGE INDICATORS", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row 1
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gemini API REST Cycles", fontSize = 11.sp, color = TextGray)
                                Text("1,420 / 5,000 queries used", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                            }
                            LinearProgressIndicator(
                                progress = { 1420f / 5000f },
                                color = AccentBlue,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                            )

                            // Row 2
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Watchlist AI Alerts Triggered", fontSize = 11.sp, color = TextGray)
                                Text("28 / Unlimited active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                            }
                            LinearProgressIndicator(
                                progress = { 0.15f },
                                color = AccentMint,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // Regional Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Localized Gateway", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

            // Coupon Field
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("UPGRADE DISCOUNT COUPE ENGINE", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponCodeInput,
                                onValueChange = { couponCodeInput = it },
                                placeholder = { Text("Enter ALPHA15 (15% off) or SHARROW50", color = TextGray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ContentTextColor, unfocusedTextColor = ContentTextColor),
                                modifier = Modifier.weight(1.8f).height(50.dp).testTag("coupon_input")
                            )
                            Button(
                                onClick = {
                                    val codeUpper = couponCodeInput.uppercase()
                                    if (codeUpper == "SHARROW50") {
                                        discountMultiplier = 0.5f
                                        couponFeedback = "Promo Applied! 50% discount registered."
                                    } else if (codeUpper == "ALPHA15") {
                                        discountMultiplier = 0.85f
                                        couponFeedback = "Promo Applied! 15% discount registered."
                                    } else {
                                        couponFeedback = "Code not recognized."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                                modifier = Modifier.weight(1.2f).height(42.dp).testTag("coupon_submit_button")
                            ) {
                                Text("Apply Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        couponFeedback?.let {
                            Text(it, color = if (it.contains("Applied")) AccentMint else AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("ACQUIRE HIGH VALUE INVESTING OUTCOMES", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // 1. Plan Pro
            item {
                GlowCard(borderColor = if (subState?.planType == "Pro") AccentBlue else BorderColor) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PRO LICENSE PLAN", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Professional Portfolio Audits", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "$symbolPr${String.format("%.2f", proRateDisplay)}/mo",
                                color = BrandNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✦ Automated sector rotation alerts & trend triggers", color = TextGray, fontSize = 12.sp)
                        Text("✦ Local Room database holding manager & PDF exporters", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedCheckoutPlan = "Pro"
                                selectedPaymentMethod = if (selectedCountry == "India") "UPI" else "Stripe"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upgrade to Active Audit License", color = Color.White)
                        }
                    }
                }
            }

            // 2. Plan Elite
            item {
                GlowCard(borderColor = if (subState?.planType == "Elite") AccentMint else BorderColor) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ELITE INTELLIGENCE PLAN", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("AI Opportunity Radar Engine", color = AccentMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "$symbolPr${String.format("%.2f", eliteRateDisplay)}/mo",
                                color = AccentMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✦ Unlimited AI Market briefings via REST Gemini key pipeline", color = TextGray, fontSize = 12.sp)
                        Text("✦ Dynamic Custom Ticker Analyst Conviction report generator", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedCheckoutPlan = "Elite"
                                selectedPaymentMethod = if (selectedCountry == "India") "UPI" else "Stripe"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Acquire Total Intelligence License", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Plan Family
            item {
                GlowCard(borderColor = if (subState?.planType == "Family") AccentOrange else BorderColor) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("FAMILY PLATFORM SHIELD", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Up to 5 Family Accounts Supported", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "$symbolPr${String.format("%.2f", familyRateDisplay)}/mo",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✦ Combined portfolio risk scoring & collaborative debate room", color = TextGray, fontSize = 12.sp)
                        Text("✦ Premium shared access with priority server bandwidth", color = TextGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedCheckoutPlan = "Family"
                                selectedPaymentMethod = if (selectedCountry == "India") "UPI" else "Stripe"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock Shared Family Plan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Checkout Gateway Simulation Dialog
            item {
                AnimatedVisibility(
                    visible = selectedCheckoutPlan != null,
                    enter = fadeIn(animationSpec = tween(350, easing = EaseOutCubic)) +
                            expandVertically(animationSpec = tween(350, easing = EaseOutCubic)),
                    exit = fadeOut(animationSpec = tween(250, easing = EaseInCubic)) +
                            shrinkVertically(animationSpec = tween(250, easing = EaseInCubic))
                ) {
                    val planName = selectedCheckoutPlan ?: ""
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PanelBg)
                                .border(2.dp, AccentBlue, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ShoppingCart, "Secure Checkout", tint = AccentBlue)
                                        Text("SECURE GATEWAY CHECKOUT", color = BrandNavy, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                    IconButton(onClick = { selectedCheckoutPlan = null }) {
                                        Icon(Icons.Default.Close, "Cancel", tint = TextGray)
                                    }
                                }

                                Divider(color = BorderColor)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Purchase License:", fontSize = 12.sp, color = TextGray)
                                    Text("$planName Plan ($selectedCountry zone)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                }

                                val activeRate = when(planName) {
                                    "Pro" -> proRateDisplay
                                    "Elite" -> eliteRateDisplay
                                    else -> familyRateDisplay
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Calculated Total Amount:", fontSize = 12.sp, color = TextGray)
                                    Text("$symbolPr${String.format("%.2f", activeRate)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentMint)
                                }

                                // UPI vs Card switcher
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val methods = if (selectedCountry == "India") listOf("UPI", "Razorpay Card") else listOf("Stripe", "PayPal")
                                    methods.forEach { meth ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selectedPaymentMethod == meth) AccentBlue.copy(alpha = 0.15f) else BorderColor.copy(alpha = 0.2f))
                                                .border(1.dp, if (selectedPaymentMethod == meth) AccentBlue else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { selectedPaymentMethod = meth }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(meth, color = ContentTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // UPI Sub-options
                                if (selectedPaymentMethod == "UPI") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        listOf("PhonePe", "Paytm", "Google Pay").forEach { upiType ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BorderColor.copy(alpha = 0.1f))
                                                    .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(upiType, fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }

                                if (!checkoutCompleted) {
                                    Button(
                                        onClick = {
                                            checkoutProcessing = true
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(1800) // simulated network handshake
                                                checkoutProcessing = false
                                                checkoutCompleted = true
                                                checkoutReceiptId = "TXN_SHW_${(100000..999999).random()}"
                                                viewModel.executePremiumUpgrade(
                                                    planName,
                                                    selectedCountry,
                                                    symbolPr,
                                                    "${String.format("%.2f", activeRate)}/month",
                                                    if (discountMultiplier < 1f) "PROMO_CODE" else null
                                                )
                                            }
                                        },
                                        enabled = !checkoutProcessing,
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                                        modifier = Modifier.fillMaxWidth().testTag("secure_checkout_execute_button")
                                    ) {
                                        if (checkoutProcessing) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("HANDSHAKING FRAUD SECURE VERIFIER...")
                                        } else {
                                            Text("AUTHORIZE SECURE PAYMENT VIA $selectedPaymentMethod", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AccentMint.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .border(1.dp, AccentMint, RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("✓ TRANSACTION COMPLETED SUCCESSFULLY!", fontWeight = FontWeight.Bold, color = AccentMint, fontSize = 11.sp)
                                            Text("System Reference ID: $checkoutReceiptId", fontSize = 10.sp, color = ContentTextColor)
                                            Text("Plan entitlement updated automatically. Sharrow invoice generated inside billing ledger.", fontSize = 10.sp, color = TextGray)
                                            Button(
                                                onClick = {
                                                    selectedCheckoutPlan = null
                                                    checkoutCompleted = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                            ) {
                                                Text("Close Secure Session", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Billing ledger receipt history
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("HISTORICAL BILLING LEDGER", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            Triple("June 2026", "Elite Plan Subscription", "Paid: ₹999.00 • Receipt generated"),
                            Triple("May 2026", "Elite Plan Subscription", "Paid: ₹999.00 • Receipt generated"),
                            Triple("April 2026", "Initial Setup License", "Paid: ₹0.00 • FREE TRIAL")
                        ).forEach { (date, desc, outcome) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(date, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text(desc, fontSize = 10.sp, color = TextGray)
                                    Text(outcome, fontSize = 9.sp, color = AccentMint, fontStyle = FontStyle.Italic)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BorderColor.copy(alpha = 0.2f))
                                        .clickable { }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("INVOICE PDF", fontSize = 9.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                            Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            // Admin Revenue Telemetry Toggle
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

        } else {
            // ==================== TAB 2: SECURITY & AUTH 2.0 ====================

            // Authentication Identity Providers List
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("CONNECTED IDENTITY PROVIDERS", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        listOf(
                            Triple("Google Cloud SSO", "Connected", "sudhakarbr2005@gmail.com"),
                            Triple("Apple Secure Sign-In", "Not Configured", "Connect Account"),
                            Triple("GitHub Developer ID", "Not Configured", "Connect Account")
                        ).forEach { (provider, status, details) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(provider, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text(details, fontSize = 10.sp, color = TextGray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (status == "Connected") AccentMint.copy(alpha = 0.15f) else BorderColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 9.sp,
                                        color = if (status == "Connected") AccentMint else AccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            // Biometric Settings
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("BIOMETRIC & DEVICE ENCRYPTION", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text("Biometric Login Flow (FaceID / Fingerprint)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                Text("Simulate system hardware verification prompt on terminal access.", fontSize = 10.sp, color = TextGray)
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { biometricEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentMint, checkedTrackColor = AccentMint.copy(alpha = 0.4f))
                            )
                        }

                        Divider(color = BorderColor.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text("Two-Factor Authentication (2FA)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                Text("Require secure TOTP verification on secondary auth devices.", fontSize = 10.sp, color = TextGray)
                            }
                            Switch(
                                checked = twoFactorEnabled,
                                onCheckedChange = { twoFactorEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentMint, checkedTrackColor = AccentMint.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            // Trusted Devices List
            item {
                GlowCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ACTIVE TRUSTED SESSION HISTORIES", color = BrandNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${sessionHistoryList.size} Devices", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        sessionHistoryList.forEachIndexed { idx, (device, location, os) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(device, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text("$location • $os", fontSize = 10.sp, color = TextGray)
                                }
                                if (idx > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentOrange.copy(alpha = 0.12f))
                                            .clickable {
                                                sessionHistoryList = sessionHistoryList.filterIndexed { i, _ -> i != idx }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("REVOKE", fontSize = 9.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentMint.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("CURRENT", fontSize = 9.sp, color = AccentMint, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Divider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockSummarySidePanel(viewModel: FinancialViewModel) {
    val selectedSymbol by viewModel.selectedStockForSummary.collectAsState()
    val summaryText by viewModel.selectedStockSummaryText.collectAsState()
    val riskText by viewModel.selectedStockRiskText.collectAsState()
    val isLoading by viewModel.isLoadingSummary.collectAsState()

    AnimatedVisibility(
        visible = selectedSymbol != null,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut()
    ) {
        val configuration = LocalConfiguration.current
        val isExpanded = configuration.screenWidthDp >= 600
        val panelWidth = if (isExpanded) 360.dp else 280.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.dismissStockSummary()
                },
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(panelWidth)
                    .background(CanvasBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .clickable(enabled = false) {}
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
                    .testTag("stock_summary_side_panel"),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Summary Icon",
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AI INSIGHT PANEL",
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = 1.2.sp,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.dismissStockSummary() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("close_summary_panel")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close panel",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    selectedSymbol?.let { symbol ->
                        val companyName = getStockFullName(symbol)
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF808080).copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = symbol,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = BrandNavy,
                                        modifier = Modifier.testTag("summary_panel_symbol")
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = companyName,
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(AccentBlue.copy(alpha = 0.12f), CircleShape)
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = AccentBlue,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Querying Gemini API...",
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "ONE-SENTENCE SUMMARY",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = summaryText.ifEmpty { "Generating detailed corporate summary..." },
                                color = ContentTextColor,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("summary_panel_text")
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(color = BorderColor, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Risk Warning",
                                    tint = AccentOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "CONCISE RISK ASSESSMENT",
                                    color = AccentOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = riskText.ifEmpty { "Evaluating sectoral & macro volatile parameters..." },
                                color = ContentTextColor,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("summary_panel_risk_text")
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = "Generative AI can make mistakes. Verify financial data prior to making real trading decisions.",
                        color = TextGray,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
