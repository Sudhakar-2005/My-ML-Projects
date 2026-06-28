package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.FinancialViewModel
import com.example.ui.viewmodel.SectorFlow
import kotlin.math.abs

@Composable
fun MoneyFlowVisualizerCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val sectorFlows by viewModel.sectorFlows.collectAsState()
    val briefing by viewModel.rotationBriefing.collectAsState()
    val isGenerating by viewModel.isGeneratingRotationBriefing.collectAsState()
    val isRefreshingFlows by viewModel.isRefreshingSectorFlows.collectAsState()

    var selectedSectorForDetail by remember { mutableStateOf<SectorFlow?>(null) }

    if (isRefreshingFlows) {
        MoneyFlowVisualizerSkeleton(modifier = modifier.testTag("money_flow_visualizer_card"))
    } else {
        GlowCard(
            borderColor = AccentBlue.copy(alpha = 0.3f),
            modifier = modifier
                .testTag("money_flow_visualizer_card")
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
                                .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📊", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "MONEY FLOW VISUALIZER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Institutional capital rotation & sector flows",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshSectorFlows() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_sector_flows_button"),
                        enabled = !isRefreshingFlows && !isGenerating
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh money flows",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = AccentBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Tap any sector block to decode underlying institutional flows & strategies.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Treemap container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("treemap_container")
            ) {
                if (sectorFlows.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading money flow configurations...", color = TextGray, fontSize = 12.sp)
                    }
                } else {
                    TreemapLayout(
                        items = sectorFlows,
                        modifier = Modifier.fillMaxSize(),
                        onSectorClick = { selectedSectorForDetail = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Gemini Rotation Intelligence Briefing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("money_flow_intelligence_section")
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
                            text = "GEMINI ROTATION INTELLIGENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (!isGenerating && briefing.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.generateRotationBriefing() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(24.dp)
                                .testTag("regenerate_rotation_briefing_button")
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
                            text = "Analyzing capital flow dynamics & order book backlogs...",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = briefing.ifEmpty { "Generating detailed sector money flow briefing..." },
                        color = ContentTextColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("money_flow_briefing_text")
                    )
                }
            }
        }
    }
}
      // Detail Dialog
    selectedSectorForDetail?.let { sector ->
        Dialog(onDismissRequest = { selectedSectorForDetail = null }) {
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(sector) {
                isVisible = true
            }

            LaunchedEffect(isVisible) {
                if (!isVisible) {
                    kotlinx.coroutines.delay(220)
                    selectedSectorForDetail = null
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(250, easing = EaseOutCubic)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(250, easing = EaseOutCubic)) +
                        slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(250, easing = EaseOutCubic)),
                exit = fadeOut(animationSpec = tween(200, easing = EaseInCubic)) +
                        scaleOut(targetScale = 0.9f, animationSpec = tween(200, easing = EaseInCubic)) +
                        slideOutVertically(targetOffsetY = { 40 }, animationSpec = tween(200, easing = EaseInCubic))
            ) {
                GlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sector.sector,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentTextColor
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (sector.percentageChange >= 0) AccentMint.copy(alpha = 0.15f)
                                        else AccentOrange.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (sector.percentageChange >= 0) "INFLOW" else "OUTFLOW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sector.percentageChange >= 0) AccentMint else AccentOrange
                                )
                            }
                        }

                        HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("NET CAPITAL FLOW", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${if (sector.netInflowMillions >= 0) "+" else ""}\$${sector.netInflowMillions}M",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (sector.netInflowMillions >= 0) AccentMint else AccentOrange
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("CAPITAL CHANGE", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${if (sector.percentageChange >= 0) "+" else ""}${sector.percentageChange}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (sector.percentageChange >= 0) AccentMint else AccentOrange
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "INSTITUTIONAL STRATEGY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sector.description,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = ContentTextColor
                            )
                        }

                        Button(
                            onClick = { isVisible = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Close Insight", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TreemapLayout(
    items: List<SectorFlow>,
    modifier: Modifier = Modifier,
    onSectorClick: (SectorFlow) -> Unit
) {
    if (items.isEmpty()) return

    // Ensure sorted by descending magnitude of flow
    val sortedItems = remember(items) {
        items.sortedByDescending { abs(it.netInflowMillions) }
    }

    Box(modifier = modifier) {
        RecursiveTreemapSplit(
            items = sortedItems,
            isHorizontal = true,
            modifier = Modifier.fillMaxSize(),
            onSectorClick = onSectorClick
        )
    }
}

@Composable
fun RecursiveTreemapSplit(
    items: List<SectorFlow>,
    isHorizontal: Boolean,
    modifier: Modifier = Modifier,
    onSectorClick: (SectorFlow) -> Unit
) {
    if (items.isEmpty()) return
    if (items.size == 1) {
        TreemapCell(
            item = items[0],
            modifier = modifier,
            onClick = { onSectorClick(items[0]) }
        )
        return
    }

    // Split the list into two parts as equally balanced by weight as possible
    val totalWeight = items.sumOf { abs(it.netInflowMillions) }
    var currentWeightSum = 0.0
    var splitIndex = 1
    var minDiff = Double.MAX_VALUE

    for (i in 1 until items.size) {
        currentWeightSum += abs(items[i - 1].netInflowMillions)
        val remainingWeight = totalWeight - currentWeightSum
        val diff = abs(currentWeightSum - remainingWeight)
        if (diff < minDiff) {
            minDiff = diff
            splitIndex = i
        }
    }

    val firstPart = items.subList(0, splitIndex)
    val secondPart = items.subList(splitIndex, items.size)

    val firstWeight = firstPart.sumOf { abs(it.netInflowMillions) }.toFloat()
    val secondWeight = secondPart.sumOf { abs(it.netInflowMillions) }.toFloat()

    val totalWeightFloat = firstWeight + secondWeight
    val firstFraction = if (totalWeightFloat > 0) firstWeight / totalWeightFloat else 0.5f
    val secondFraction = 1f - firstFraction

    if (isHorizontal) {
        Row(modifier = modifier) {
            RecursiveTreemapSplit(
                items = firstPart,
                isHorizontal = !isHorizontal,
                modifier = Modifier
                    .weight(firstFraction)
                    .fillMaxHeight(),
                onSectorClick = onSectorClick
            )
            RecursiveTreemapSplit(
                items = secondPart,
                isHorizontal = !isHorizontal,
                modifier = Modifier
                    .weight(secondFraction)
                    .fillMaxHeight(),
                onSectorClick = onSectorClick
            )
        }
    } else {
        Column(modifier = modifier) {
            RecursiveTreemapSplit(
                items = firstPart,
                isHorizontal = !isHorizontal,
                modifier = Modifier
                    .weight(firstFraction)
                    .fillMaxWidth(),
                onSectorClick = onSectorClick
            )
            RecursiveTreemapSplit(
                items = secondPart,
                isHorizontal = !isHorizontal,
                modifier = Modifier
                    .weight(secondFraction)
                    .fillMaxWidth(),
                onSectorClick = onSectorClick
            )
        }
    }
}

@Composable
fun TreemapCell(
    item: SectorFlow,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isDarkThemeState
    val isPositive = item.netInflowMillions >= 0

    // Compute beautiful colors based on theme and polarity
    val cellBg = when {
        isPositive -> if (isDark) Color(0xFF0F3A20) else Color(0xFFE8FDF0)
        else -> if (isDark) Color(0xFF4A1212) else Color(0xFFFDF2F2)
    }

    val cellBorder = when {
        isPositive -> if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
        else -> if (isDark) Color(0xFFB71C1C) else Color(0xFFFFCDD2)
    }

    val labelColor = when {
        isPositive -> if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        else -> if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
    }

    val valueColor = if (isDark) Color.White else Color(0xFF1E293B)

    BoxWithConstraints(
        modifier = modifier
            .padding(1.5.dp)
            .background(cellBg, RoundedCornerShape(6.dp))
            .border(1.dp, cellBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
            .testTag("treemap_cell_${item.sector.replace(" ", "_")}")
    ) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sector label (show size depends on bounds size)
            if (width > 80.dp && height > 35.dp) {
                Text(
                    text = item.sector,
                    color = valueColor,
                    fontSize = if (width > 120.dp) 11.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Value + Pct
            if (width > 60.dp && height > 20.dp) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (item.netInflowMillions >= 0) "+" else ""}\$${item.netInflowMillions.toInt()}M",
                        color = valueColor.copy(alpha = 0.85f),
                        fontSize = if (width > 120.dp) 10.sp else 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "(${if (item.percentageChange >= 0) "+" else ""}${item.percentageChange}%)",
                        color = labelColor,
                        fontSize = if (width > 120.dp) 10.sp else 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
