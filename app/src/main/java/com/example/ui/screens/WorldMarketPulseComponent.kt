package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.viewmodel.WorldMarketPulseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMarketPulseCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val pulseItems by viewModel.worldMarketPulse.collectAsState()
    val isRefreshing by viewModel.isRefreshingWorldMarketPulse.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedItemForDetail by remember { mutableStateOf<WorldMarketPulseItem?>(null) }

    val filters = listOf("ALL", "INDICES", "COMMODITIES", "CURRENCIES", "CRYPTO")

    // Filter items based on selection
    val filteredItems = remember(pulseItems, selectedFilter) {
        pulseItems.filter { item ->
            when (selectedFilter) {
                "INDICES" -> item.region in listOf("India", "USA", "Europe", "Japan", "China")
                "COMMODITIES" -> item.region == "Commodities"
                "CURRENCIES" -> item.region == "Currencies"
                "CRYPTO" -> item.region == "Crypto"
                else -> true
            }
        }
    }

    if (isRefreshing || pulseItems.isEmpty()) {
        WorldMarketPulseSkeleton(modifier = modifier.testTag("world_market_pulse_card"))
    } else {
        GlowCard(
            borderColor = AccentBlue.copy(alpha = 0.3f),
            modifier = modifier
                .testTag("world_market_pulse_card")
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
                            Text("🌐", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "WORLD MARKET PULSE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandNavy,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Real-time global macro snapshots & AI summaries",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshWorldMarketPulse() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_world_pulse_button"),
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh world pulse",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White,
                            containerColor = BorderColor.copy(alpha = 0.15f),
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = BorderColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("filter_chip_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pulse Items Grid/List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trackers available for selection.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    filteredItems.forEach { item ->
                        PulseItemRow(
                            item = item,
                            onClick = { selectedItemForDetail = item }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom informational banner
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
                    text = "Tap any global asset row to decode premium Sharrow AI macro strategy.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue
                )
            }
        }
    }

    // Detail dialog for clicked item
    selectedItemForDetail?.let { item ->
        Dialog(onDismissRequest = { selectedItemForDetail = null }) {
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(item) {
                isVisible = true
            }

            LaunchedEffect(isVisible) {
                if (!isVisible) {
                    kotlinx.coroutines.delay(220)
                    selectedItemForDetail = null
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
                            Column {
                                Text(
                                    text = item.region.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = item.indexName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ContentTextColor
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (item.changePercent >= 0) AccentMint.copy(alpha = 0.15f)
                                        else AccentOrange.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.changePercent >= 0) AccentMint else AccentOrange
                                )
                            }
                        }

                        HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT VALUE / SPOT", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                Text(
                                    text = item.price,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ContentTextColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("24H CHANGE", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${if (item.changePercent >= 0) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.changePercent >= 0) AccentMint else AccentOrange
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BorderColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✨", fontSize = 12.sp)
                                Text(
                                    text = "SHARROW AI MACRO BRIEFING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.aiSummary,
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
}

@Composable
fun PulseItemRow(
    item: WorldMarketPulseItem,
    onClick: () -> Unit
) {
    val isDark = isDarkThemeState
    val isPositive = item.changePercent >= 0

    val itemIcon = when (item.indexName) {
        "Nifty 50" -> "🇮🇳"
        "S&P 500" -> "🇺🇸"
        "Euro Stoxx 50" -> "🇪🇺"
        "Nikkei 225" -> "🇯🇵"
        "Shanghai Comp" -> "🇨🇳"
        "Gold" -> "✨"
        "Brent Crude Oil" -> "🛢️"
        "USD/INR" -> "💵"
        "EUR/USD" -> "💶"
        "Bitcoin (BTC)" -> "🪙"
        else -> "📈"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) PanelBg.copy(alpha = 0.6f) else Color.White)
            .border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("world_pulse_row_${item.indexName.replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon / Emoji
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isPositive) AccentMint.copy(alpha = 0.08f)
                    else AccentOrange.copy(alpha = 0.08f),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (isPositive) AccentMint.copy(alpha = 0.2f)
                    else AccentOrange.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(itemIcon, fontSize = 16.sp)
        }

        // Mid section - name and AI summary
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.indexName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ContentTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Box(
                    modifier = Modifier
                        .background(BorderColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.region.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.aiSummary,
                fontSize = 11.sp,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right section - price and change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.price,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ContentTextColor
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${if (isPositive) "+" else ""}${String.format("%.2f", item.changePercent)}%",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isPositive) AccentMint else AccentOrange
            )
        }
    }
}
