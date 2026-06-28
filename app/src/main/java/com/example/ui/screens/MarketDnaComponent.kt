package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinancialViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MarketDnaDashboardCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val marketDna by viewModel.marketDna.collectAsState()
    val isRefreshing by viewModel.isRefreshingMarketDna.collectAsState()

    if (isRefreshing) {
        MarketDnaSkeleton(modifier = modifier.testTag("market_dna_card"))
    } else {
        GlowCard(
            borderColor = AccentMint.copy(alpha = 0.3f),
            modifier = modifier
                .testTag("market_dna_card")
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(AccentMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧬", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "MARKET DNA SENTIMENT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Real-time Neural Intelligence & Flow Analysis",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshMarketDna() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_market_dna_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Market DNA",
                            tint = AccentMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Gauges Row (Responsive Adaptive Layout)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 400.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SemiCircularGauge(
                            score = marketDna.fearGreedScore,
                            label = marketDna.fearGreedLabel,
                            title = "FEAR & GREED INDEX",
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("fear_greed_gauge")
                        )
                        
                        CircularProgressGauge(
                            score = marketDna.liquidityScore,
                            title = "LIQUIDITY SCORE",
                            activeColor = AccentBlue,
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("liquidity_gauge")
                        )
                        
                        CircularProgressGauge(
                            score = marketDna.momentumScore,
                            title = "MOMENTUM SCORE",
                            activeColor = AccentMint,
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("momentum_gauge")
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SemiCircularGauge(
                            score = marketDna.fearGreedScore,
                            label = marketDna.fearGreedLabel,
                            title = "FEAR & GREED INDEX",
                            modifier = Modifier
                                .testTag("fear_greed_gauge")
                                .fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressGauge(
                                score = marketDna.liquidityScore,
                                title = "LIQUIDITY SCORE",
                                activeColor = AccentBlue,
                                modifier = Modifier
                                    .testTag("liquidity_gauge")
                                    .weight(1f)
                            )
                            
                            CircularProgressGauge(
                                score = marketDna.momentumScore,
                                title = "MOMENTUM SCORE",
                                activeColor = AccentMint,
                                modifier = Modifier
                                    .testTag("momentum_gauge")
                                    .weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Badges Section (Flow Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarketDnaBadge(
                    label = "Institutions",
                    value = marketDna.institutionalBuying,
                    color = AccentBlue
                )
                MarketDnaBadge(
                    label = "Retail",
                    value = marketDna.retailActivity,
                    color = AccentMint
                )
                MarketDnaBadge(
                    label = "Volatility",
                    value = marketDna.volatilityLevel,
                    color = AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Summary Info Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F0FF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFADC6FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Neural Analysis Summary",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004786)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = marketDna.aiSummary,
                        color = Color(0xFF00305E),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            val explanation by viewModel.marketDnaExplanation.collectAsState()
            val isGenerating by viewModel.isGeneratingDnaExplanation.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_dna_explanation_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 14.sp)
                        Text(
                            text = "GEMINI SENTIMENT DECODER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    if (!isGenerating && explanation.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.generateMarketDnaExplanation() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(24.dp)
                                .testTag("regenerate_explanation_button")
                        ) {
                            Text("Re-Analyze", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isGenerating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            color = AccentBlue,
                            trackColor = BorderColor.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                        Text(
                            text = "Gemini decoding underlying macro flows & behavioral indicators...",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = explanation.ifEmpty { "Generating detailed daily explanation..." },
                        color = ContentTextColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_dna_explanation_text")
                    )
                }
            }
        }
    }
    }
}

@Composable
fun SemiCircularGauge(
    score: Int,
    label: String,
    title: String,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_score_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(130.dp, 75.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val needleColor = BrandNavy
            val trackColor = BorderColor.copy(alpha = 0.5f)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val radius = width / 2f
                val strokeWidth = 10.dp.toPx()
                
                // Draw background arc
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Draw colored segments
                val colors = listOf(
                    Color(0xFFEF4444), // Red
                    Color(0xFFF97316), // Orange
                    Color(0xFFFBBF24), // Yellow
                    Color(0xFF4ADE80), // Mint
                    Color(0xFF60A5FA)  // Blue
                )
                
                val segmentSweep = 180f / colors.size
                for (i in colors.indices) {
                    drawArc(
                        color = colors[i].copy(alpha = 0.85f),
                        startAngle = 180f + (i * segmentSweep),
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
                
                // Draw needle line
                val angleRad = (180f + (animatedScore / 100f) * 180f) * (PI / 180f)
                val needleLength = radius - strokeWidth - 6.dp.toPx()
                val centerX = width / 2f
                val centerY = height // bottom center
                
                val endX = centerX + needleLength * cos(angleRad).toFloat()
                val endY = centerY + needleLength * sin(angleRad).toFloat()
                
                drawLine(
                    color = needleColor,
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                drawCircle(
                    color = needleColor,
                    radius = 7.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${score}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ContentTextColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    label.lowercase().contains("extreme greed") -> Color(0xFF60A5FA)
                    label.lowercase().contains("greed") -> Color(0xFF4ADE80)
                    label.lowercase().contains("neutral") -> Color(0xFFFBBF24)
                    label.lowercase().contains("fear") -> Color(0xFFF97316)
                    else -> Color(0xFFEF4444)
                }
            )
        }
    }
}

@Composable
fun CircularProgressGauge(
    score: Int,
    title: String,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "circular_score_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = BorderColor.copy(alpha = 0.5f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val strokeWidth = 7.dp.toPx()
                
                // Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Progress
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = (animatedScore / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${score}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = ContentTextColor
                )
                Text(
                    text = "pts",
                    fontSize = 9.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RowScope.MarketDnaBadge(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
