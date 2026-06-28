package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(viewModel: FinancialViewModel, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val localBorder = BorderColor

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. HERO HEADER WITH GLOWING ACCENTS
        item {
            MotionDiv(delayMillis = 0, yOffset = 30f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AccentBlue.copy(alpha = 0.12f),
                                    CanvasBg,
                                    AccentMint.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(AccentBlue.copy(alpha = 0.3f), localBorder.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
                        .testTag("landing_hero_section")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .background(AccentBlue.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ SHARROW.AI V1.5 PREMIUM INTELLIGENCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Institutional Alpha.",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandNavy,
                            textAlign = TextAlign.Center,
                            lineHeight = 38.sp
                        )
                        Text(
                            text = "Powered by Neural Agents.",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "The ultimate AI Command Center for your capital. Sharrow.ai autonomously monitors market filings, tracks institutional money flow, debates thesis positions, and guides your portfolio with professional precision.",
                            fontSize = 13.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Statistics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("₹1,240 Cr+", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AccentMint)
                                Text("ALGO VOLUME", fontSize = 9.sp, color = TextGray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("94.2%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AccentBlue)
                                Text("AI COMPLIANCE", fontSize = 9.sp, color = TextGray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("8.6x", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AccentMint)
                                Text("S&P OUTPERFORM", fontSize = 9.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        }

        // 2. INTERACTIVE "ASK AI" DEMO (VISITOR INPUT)
        item {
            MotionDiv(delayMillis = 100, yOffset = 20f) {
                val landingAskAiResponse by viewModel.landingAskAiResponse.collectAsState()
                val isLoadingLandingAskAi by viewModel.isLoadingLandingAskAi.collectAsState()
                var questionInput by remember { mutableStateOf("") }
                val keyboardController = LocalSoftwareKeyboardController.current

                GlowCard(
                    borderColor = AccentMint.copy(alpha = 0.4f),
                    modifier = Modifier.testTag("landing_ask_ai_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AccentMint.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💬", fontSize = 12.sp)
                            }
                            Text(
                                text = "ASK SHARROW AI (FREE DEMO)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Test our institutional engine immediately. Type any financial query or ticker below to query real-time sentiment analysis.",
                            fontSize = 11.sp,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            placeholder = { Text("Is BEL a good buy for defense allocation?", fontSize = 12.sp, color = TextGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                viewModel.askAiLandingQuestion(questionInput)
                                keyboardController?.hide()
                            }),
                            trailingIcon = {
                                if (isLoadingLandingAskAi) {
                                    CircularProgressIndicator(color = AccentMint, modifier = Modifier.size(18.dp))
                                } else {
                                    IconButton(
                                        onClick = {
                                            viewModel.askAiLandingQuestion(questionInput)
                                            keyboardController?.hide()
                                        },
                                        enabled = questionInput.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Send, "Send", tint = AccentMint)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ContentTextColor,
                                unfocusedTextColor = ContentTextColor,
                                focusedBorderColor = AccentMint,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("landing_ask_ai_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // AI Response Container
                        if (landingAskAiResponse == null && !isLoadingLandingAskAi) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BorderColor.copy(alpha = 0.08f))
                                    .border(1.dp, BorderColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💡", fontSize = 14.sp)
                                    Text(
                                        text = "Ask your first investment question.",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        landingAskAiResponse?.let { responseText ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentMint.copy(alpha = 0.08f))
                                    .border(1.dp, AccentMint.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .testTag("landing_ask_ai_response")
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🧠", fontSize = 13.sp)
                                        Text(
                                            "SHARROW INTELLIGENCE VERDICT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentMint
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = responseText,
                                        fontSize = 11.sp,
                                        color = ContentTextColor,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Preset suggest questions
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Compare TCS vs INFY",
                                "Suggest tech hedges"
                            ).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BorderColor.copy(alpha = 0.15f))
                                        .clickable {
                                            questionInput = preset
                                            viewModel.askAiLandingQuestion(preset)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(preset, fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. LIVE MARKET RADAR PREVIEW
        item {
            FramerMotionEntrance(delayMillis = 200, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TrendingUp, "Radar Preview", tint = AccentBlue)
                                Text(
                                    "LIVE MARKET RADAR PREVIEW",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(AccentMint.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = AccentMint)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Market Pulse preview row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("NIFTY 50", "23,450", "+1.12%"),
                                Triple("SENSEX", "77,200", "+0.95%"),
                                Triple("NASDAQ", "17,890", "+1.42%")
                            ).forEach { (index, value, change) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(BorderColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(index, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                    Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = ContentTextColor)
                                    Text(change, fontSize = 9.sp, color = AccentMint, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. INTERACTIVE PORTFOLIO DOCTOR DEMO (Allocation Simulator)
        item {
            FramerMotionEntrance(delayMillis = 300, yOffset = 20f) {
                var techAllocation by remember { mutableStateOf(70f) }
                val energyAllocation = remember(techAllocation) { (100f - techAllocation).coerceIn(0f, 100f) }

                GlowCard(borderColor = AccentBlue.copy(alpha = 0.4f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🩺", fontSize = 16.sp)
                            Text(
                                "PORTFOLIO DOCTOR ALLOCATION SIMULATOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Drag the slider to adjust allocation. Sharrow.ai immediately diagnoses concentration risks.",
                            fontSize = 11.sp,
                            color = TextGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Diagnosis Results
                        val score = remember(techAllocation) {
                            val base = 100
                            val techRisk = if (techAllocation > 50) (techAllocation - 50) * 1.2f else 0f
                            (base - techRisk).toInt().coerceIn(30, 100)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Technology Sector: ${techAllocation.toInt()}%", fontSize = 11.sp, color = ContentTextColor, fontWeight = FontWeight.Bold)
                                Text("Energy & Metal Sector: ${energyAllocation.toInt()}%", fontSize = 11.sp, color = TextGray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (score >= 80) AccentMint.copy(alpha = 0.15f)
                                            else if (score >= 60) AccentBlue.copy(alpha = 0.15f)
                                            else AccentOrange.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$score",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (score >= 80) AccentMint else if (score >= 60) AccentBlue else AccentOrange
                                    )
                                }
                                Text("DIAGNOSIS SCORE", fontSize = 7.sp, color = TextGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Slider(
                            value = techAllocation,
                            onValueChange = { techAllocation = it },
                            valueRange = 10f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("landing_doctor_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Diagnostic text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = when {
                                    score >= 80 -> "✅ Portfolio is highly diversified. You have secure capital safeguards."
                                    score >= 60 -> "⚠️ Moderate IT concentration. Sharrow AI suggests adding 10% non-cyclical energy plays to hedge rate changes."
                                    else -> "❌ CRITICAL EXPOSURE WARNING! Extreme technology concentration (over 70%) renders you highly vulnerable to offshore rate cycles. Shift 15% immediately."
                                },
                                fontSize = 11.sp,
                                color = ContentTextColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 5. AI OPPORTUNITY PREVIEW
        item {
            FramerMotionEntrance(delayMillis = 400, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "AI OPPORTUNITY RANKING PREVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        listOf(
                            Triple("BEL", "94% Conviction • Defense Tech", "Robust order pipeline expansion driven by sovereign budget mandates."),
                            Triple("TCS", "88% Conviction • IT Cloud", "Expanding banking digital transformation contracts under multi-year agreements.")
                        ).forEach { (symbol, conviction, info) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(BorderColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentMint)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(conviction, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text(info, fontSize = 10.sp, color = TextGray, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. CUSTOMER JOURNEY ANIMATION (Novice -> Compound Master)
        item {
            FramerMotionEntrance(delayMillis = 450, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "YOUR JOURNEY TO AUTONOMOUS WEALTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val steps = listOf(
                            Pair("Phase 1: Diagnosis", "Connect your existing portfolio. Sharrow instantly audits concentration issues, hidden risks, and missing allocation shields."),
                            Pair("Phase 2: Active Radar", "Neural agents scan local and global filings daily to pinpoint 90%+ high-conviction momentum breakout targets."),
                            Pair("Phase 3: Alpha Replay", "Evaluate historical choices, simulate following AI advice vs ignoring, and eliminate emotional decision leakages.")
                        )

                        steps.forEachIndexed { i, (phase, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(AccentBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${i + 1}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(phase, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text(desc, fontSize = 10.sp, color = TextGray, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. COMPACT COMPANY COMPARISON TOOL
        item {
            FramerMotionEntrance(delayMillis = 500, yOffset = 20f) {
                var selectedComparison by remember { mutableStateOf("TCS_INFY") }

                GlowCard(borderColor = AccentMint.copy(alpha = 0.35f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "AI SIDE-BY-SIDE METRIC COMPASS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // Mode selectors
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "TCS_INFY" to "TCS vs INFY",
                                "AAPL_MSFT" to "AAPL vs MSFT"
                            ).forEach { (key, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedComparison == key) AccentBlue else BorderColor.copy(alpha = 0.2f))
                                        .clickable { selectedComparison = key }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedComparison == key) Color.White else ContentTextColor
                                    )
                                }
                            }
                        }

                        // Metric tables
                        val metrics = if (selectedComparison == "TCS_INFY") {
                            listOf(
                                Triple("P/E Ratio", "30.4 (TCS)", "26.8 (INFY)"),
                                Triple("AI Conviction", "88% (TCS - Hold)", "82% (INFY - Accumulate)"),
                                Triple("Net Cash flow", "Strong Core", "Vulnerable to Western orders")
                            )
                        } else {
                            listOf(
                                Triple("P/E Ratio", "31.2 (AAPL)", "35.6 (MSFT)"),
                                Triple("AI Conviction", "91% (AAPL - Strong Buy)", "95% (MSFT - Heavy Buy)"),
                                Triple("Tech Hedge", "Device Margin Stronghold", "SaaS Cloud dominance")
                            )
                        }

                        metrics.forEach { (metric, v1, v2) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, BorderColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(metric, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(v1, fontSize = 10.sp, color = AccentMint, fontWeight = FontWeight.Bold)
                                    Text("|", fontSize = 10.sp, color = BorderColor)
                                    Text(v2, fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. INTERACTIVE FEATURE SHOWCASE
        item {
            FramerMotionEntrance(delayMillis = 550, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "SHARROW ENGINE CAPABILITIES Showcase",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val capabilities = listOf(
                            Triple("🤖 AI Market Radar", "Live briefings, daily macro reports, global pulse updates, confidence scoring.", AccentMint),
                            Triple("🩺 Portfolio Doctor", "Deep Concentration Audit, investor IQ scoring, suggesting rebalances.", AccentBlue),
                            Triple("👥 AI Debate Engine", "Interactive thesis cross-examiner between Bulls & Bears to filter out noise.", AccentMint),
                            Triple("⏳ Investment Timeline Replay", "Historical outcome comparison mapping simulated compounding paths.", AccentBlue)
                        )

                        capabilities.forEach { (title, desc, colorVal) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .padding(top = 6.dp)
                                        .background(colorVal, CircleShape)
                                )
                                Column {
                                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor)
                                    Text(desc, fontSize = 10.sp, color = TextGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. PLATFORM ROADMAP
        item {
            FramerMotionEntrance(delayMillis = 600, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "SHARROW GLOBAL ROADMAP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Pair("Q3 2026", "Razorpay India UPI checkout & localized regional pricing structures."),
                                Pair("Q4 2026", "Deep Multi-Agent Chat Memory & proactive Copilot alerts."),
                                Pair("Q1 2027", "Hilt Enterprise integrations and high-fidelity global visualizers.")
                            ).forEach { (quarter, details) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(quarter, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(details, fontSize = 9.sp, color = TextGray, lineHeight = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. LOCALIZED PRICING PREVIEW (India + International)
        item {
            FramerMotionEntrance(delayMillis = 650, yOffset = 20f) {
                var billingCycleIsYearly by remember { mutableStateOf(false) }
                var couponCodeInput by remember { mutableStateOf("") }
                var activeDiscountPercent by remember { mutableStateOf(0f) }

                GlowCard(borderColor = AccentBlue.copy(alpha = 0.45f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "PRICING ENGINE & BILLING PORTAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy
                            )

                            // Cycle switcher
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BorderColor.copy(alpha = 0.15f))
                                    .clickable { billingCycleIsYearly = !billingCycleIsYearly }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly",
                                    fontSize = 8.sp,
                                    fontWeight = if (!billingCycleIsYearly) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!billingCycleIsYearly) AccentBlue else TextGray,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(
                                    text = "Yearly",
                                    fontSize = 8.sp,
                                    fontWeight = if (billingCycleIsYearly) FontWeight.Bold else FontWeight.Normal,
                                    color = if (billingCycleIsYearly) AccentBlue else TextGray,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val baseIndiaMonthly = 2499f
                        val baseIntMonthly = 29.99f

                        val discountMultiplier = 1f - activeDiscountPercent

                        val displayIndiaPrice = if (billingCycleIsYearly) {
                            (baseIndiaMonthly * 12f * 0.8f * discountMultiplier).toInt()
                        } else {
                            (baseIndiaMonthly * discountMultiplier).toInt()
                        }

                        val displayIntPrice = if (billingCycleIsYearly) {
                            String.format("%.2f", baseIntMonthly * 12f * 0.8f * discountMultiplier)
                        } else {
                            String.format("%.2f", baseIntMonthly * discountMultiplier)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Indian regional price
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BorderColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("INDIA REGIONAL (UPI)", fontSize = 8.sp, color = TextGray)
                                Text(
                                    text = "₹${displayIndiaPrice}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentMint
                                )
                                Text(if (billingCycleIsYearly) "/ year (20% off)" else "/ month", fontSize = 8.sp, color = TextGray)
                            }

                            // Global Stripe card price
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(BorderColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("INTERNATIONAL CARD", fontSize = 8.sp, color = TextGray)
                                Text(
                                    text = "$${displayIntPrice}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentBlue
                                )
                                Text(if (billingCycleIsYearly) "/ year (20% off)" else "/ month", fontSize = 8.sp, color = TextGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Coupon input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponCodeInput,
                                onValueChange = { couponCodeInput = it.uppercase() },
                                label = { Text("Coupon Code (e.g. ALPHA15)", fontSize = 10.sp, color = TextGray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ContentTextColor,
                                    unfocusedTextColor = ContentTextColor,
                                    focusedBorderColor = AccentBlue
                                ),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("landing_coupon_input")
                            )

                            Button(
                                onClick = {
                                    if (couponCodeInput == "ALPHA15") {
                                        activeDiscountPercent = 0.15f
                                    } else if (couponCodeInput == "SHARROW50") {
                                        activeDiscountPercent = 0.50f
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier.height(44.dp).testTag("landing_coupon_button")
                            ) {
                                Text("APPLY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (activeDiscountPercent > 0f) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ Coupon code applied! Captured extra ${(activeDiscountPercent * 100).toInt()}% off discount.",
                                color = AccentMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 11. FAQ ACCORDION SECTION
        item {
            FramerMotionEntrance(delayMillis = 700, yOffset = 20f) {
                GlowCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "FREQUENTLY ASKED QUESTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val faqs = listOf(
                            Pair("Is my portfolio data secure on Sharrow?", "Yes, Sharrow.ai uses local JVM execution with standard Android SQLite Room encryption. No raw holding records are ever sent to external networks except anonymous vectors passed to Gemini."),
                            Pair("How does the Investment Replay simulator work?", "It utilizes historic price datasets of major equities over the past 12 months, mapping Sharrow recommendations with compound mathematical projections.")
                        )

                        faqs.forEach { (question, answer) ->
                            var expanded by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, BorderColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(question, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentTextColor, modifier = Modifier.weight(0.9f))
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle",
                                        tint = TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(answer, fontSize = 10.sp, color = TextGray, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 12. PREMIUM FOOTER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SHARROW.AI INC. © 2026",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Autonomous quantitative trading and predictive models are for informational and coaching purposes only. Please execute trades carefully under registered advisor consultation.",
                    fontSize = 9.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Terms of Use", fontSize = 9.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                    Text("Privacy Policy", fontSize = 9.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                    Text("Support Command", fontSize = 9.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
