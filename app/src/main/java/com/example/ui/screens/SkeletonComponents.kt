package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable pulsing shimmer block mimicking the exact look of text or components.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = Dp.Unspecified,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    val baseColor = if (isDarkThemeState) Color(0xFF262930) else Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .background(baseColor.copy(alpha = alpha), shape)
    )
}

/**
 * Skeleton Loader matching the Daily AI Alpha Brief card layout.
 */
@Composable
fun AlphaReportSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = AccentMint.copy(alpha = 0.15f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with Icon and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
                    ShimmerPlaceholder(width = 150.dp, height = 16.dp)
                }
                ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Paragraph lines
            ShimmerPlaceholder(width = 180.dp, height = 18.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 14.dp)
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.9f), height = 14.dp)
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.95f), height = 14.dp)
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.75f), height = 14.dp)

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Three Columns Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(0.8f, 0.9f, 0.7f).forEach { widthPct ->
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerPlaceholder(width = 80.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(widthPct), height = 16.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Callout Takeaway Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkThemeState) Color(0xFF1E2620) else Color(0xFFE8F5E9))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerPlaceholder(width = 60.dp, height = 12.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    ShimmerPlaceholder(modifier = Modifier.weight(1f), height = 12.dp)
                }
            }
        }
    }
}

/**
 * Skeleton Loader matching the Market DNA Sentiment Gauges Card layout.
 */
@Composable
fun MarketDnaSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = AccentMint.copy(alpha = 0.15f),
        modifier = modifier.fillMaxWidth()
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
                    ShimmerPlaceholder(width = 30.dp, height = 30.dp, shape = RoundedCornerShape(8.dp))
                    Column {
                        ShimmerPlaceholder(width = 140.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 220.dp, height = 10.dp)
                    }
                }
                ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wide vs Compact adaptive row for gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Semicircular Gauge Skeleton
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerPlaceholder(width = 120.dp, height = 60.dp, shape = RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    ShimmerPlaceholder(width = 80.dp, height = 12.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerPlaceholder(width = 100.dp, height = 8.dp)
                }

                // Two Circular Gauges Skeletons
                listOf("LIQUIDITY SCORE", "MOMENTUM SCORE").forEach { title ->
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ShimmerPlaceholder(width = 64.dp, height = 64.dp, shape = CircleShape)
                        Spacer(modifier = Modifier.height(12.dp))
                        ShimmerPlaceholder(width = 70.dp, height = 10.dp)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader matching the World Market Pulse macro items list layout.
 */
@Composable
fun WorldMarketPulseSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = AccentBlue.copy(alpha = 0.15f),
        modifier = modifier.fillMaxWidth()
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
                    ShimmerPlaceholder(width = 30.dp, height = 30.dp, shape = RoundedCornerShape(8.dp))
                    Column {
                        ShimmerPlaceholder(width = 150.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 200.dp, height = 10.dp)
                    }
                }
                ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Filters Skeletons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(40.dp, 60.dp, 80.dp, 70.dp, 50.dp).forEach { pillWidth ->
                    ShimmerPlaceholder(width = pillWidth, height = 26.dp, shape = RoundedCornerShape(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse rows Skeletons (List of 3 items)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
                            Column {
                                ShimmerPlaceholder(width = 90.dp, height = 12.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                ShimmerPlaceholder(width = 50.dp, height = 10.dp)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                ShimmerPlaceholder(width = 60.dp, height = 12.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                ShimmerPlaceholder(width = 40.dp, height = 10.dp)
                            }
                            ShimmerPlaceholder(width = 50.dp, height = 22.dp, shape = RoundedCornerShape(6.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader matching the Money Flow Visualizer layout.
 */
@Composable
fun MoneyFlowVisualizerSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = AccentBlue.copy(alpha = 0.15f),
        modifier = modifier.fillMaxWidth()
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
                    ShimmerPlaceholder(width = 30.dp, height = 30.dp, shape = RoundedCornerShape(8.dp))
                    Column {
                        ShimmerPlaceholder(width = 160.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 180.dp, height = 10.dp)
                    }
                }
                ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progressive flow horizontal bars (List of 3 rows)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(3) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ShimmerPlaceholder(width = 80.dp, height = 12.dp)
                            ShimmerPlaceholder(width = 50.dp, height = 12.dp)
                        }
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 8.dp, shape = RoundedCornerShape(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Rotation thesis briefing skeleton
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerPlaceholder(width = 120.dp, height = 11.dp)
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 13.dp)
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.95f), height = 13.dp)
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.7f), height = 13.dp)
            }
        }
    }
}

/**
 * Skeleton Loader matching the Stock Performance Line Chart card.
 */
@Composable
fun StockPerformanceLineChartSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = BorderColor.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerPlaceholder(width = 20.dp, height = 20.dp, shape = CircleShape)
                    ShimmerPlaceholder(width = 160.dp, height = 14.dp)
                }
                ShimmerPlaceholder(width = 60.dp, height = 20.dp, shape = RoundedCornerShape(4.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main simulated line chart drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkThemeState) Color(0xFF13151A) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerPlaceholder(width = 100.dp, height = 12.dp)
                    ShimmerPlaceholder(width = 240.dp, height = 8.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom X-axis labels Skeletons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    ShimmerPlaceholder(width = 40.dp, height = 10.dp)
                }
            }
        }
    }
}

/**
 * Skeleton Loader matching the News Articles card layout in the News Scanner feed.
 */
@Composable
fun NewsArticleItemSkeleton() {
    GlowCard {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category tag and Source row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPlaceholder(width = 70.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
                ShimmerPlaceholder(width = 90.dp, height = 10.dp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline skeleton
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 16.dp)
            Spacer(modifier = Modifier.height(6.dp))
            ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.9f), height = 16.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row with date and read more action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPlaceholder(width = 80.dp, height = 10.dp)
                ShimmerPlaceholder(width = 100.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Skeleton Loader matching the Portfolio Doctor Review card layout.
 */
@Composable
fun PortfolioDoctorSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = AccentMint.copy(alpha = 0.2f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ShimmerPlaceholder(width = 24.dp, height = 24.dp, shape = CircleShape)
                    ShimmerPlaceholder(width = 180.dp, height = 15.dp)
                }
                ShimmerPlaceholder(width = 80.dp, height = 22.dp, shape = RoundedCornerShape(11.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Circular dial and status rows row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPlaceholder(width = 90.dp, height = 90.dp, shape = CircleShape)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerPlaceholder(width = 120.dp, height = 12.dp)
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 10.dp)
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.85f), height = 10.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Feedback list items skeletons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShimmerPlaceholder(width = 16.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
                        ShimmerPlaceholder(modifier = Modifier.weight(1f), height = 12.dp)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader matching the Watchlist Table rows list layout.
 */
@Composable
fun WatchlistSkeleton(modifier: Modifier = Modifier) {
    GlowCard(
        borderColor = BorderColor.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPlaceholder(width = 140.dp, height = 14.dp)
                ShimmerPlaceholder(width = 80.dp, height = 24.dp, shape = RoundedCornerShape(6.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Column Names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerPlaceholder(width = 60.dp, height = 10.dp)
                ShimmerPlaceholder(width = 50.dp, height = 10.dp)
                ShimmerPlaceholder(width = 70.dp, height = 10.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Row items lists (List of 3 items)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerPlaceholder(width = 40.dp, height = 14.dp, shape = RoundedCornerShape(4.dp))
                            Column {
                                ShimmerPlaceholder(width = 80.dp, height = 11.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                ShimmerPlaceholder(width = 50.dp, height = 9.dp)
                            }
                        }
                        ShimmerPlaceholder(width = 60.dp, height = 12.dp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerPlaceholder(width = 40.dp, height = 12.dp)
                            ShimmerPlaceholder(width = 50.dp, height = 20.dp, shape = RoundedCornerShape(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResearchReportSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerPlaceholder(width = 160.dp, height = 12.dp)
            ShimmerPlaceholder(width = 80.dp, height = 20.dp, shape = RoundedCornerShape(4.dp))
        }

        // Executive Overview Box Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BorderColor.copy(alpha = 0.12f))
                .border(1.dp, BorderColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerPlaceholder(width = 100.dp, height = 11.dp)
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(), height = 12.dp)
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.95f), height = 12.dp)
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.85f), height = 12.dp)
            }
        }

        // SWOT Grid Shimmer (2x2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(100.dp).background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(width = 60.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 110.dp, height = 8.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 90.dp, height = 8.dp)
                    }
                }
                Box(modifier = Modifier.weight(1f).height(100.dp).background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(width = 60.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 110.dp, height = 8.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 90.dp, height = 8.dp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(100.dp).background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(width = 60.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 110.dp, height = 8.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 90.dp, height = 8.dp)
                    }
                }
                Box(modifier = Modifier.weight(1f).height(100.dp).background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerPlaceholder(width = 60.dp, height = 10.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 110.dp, height = 8.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(width = 90.dp, height = 8.dp)
                    }
                }
            }
        }
    }
}
