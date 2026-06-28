package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinancialViewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween

enum class DashboardTab {
    LANDING, RADAR, PORTFOLIO, SCANNER, WATCHLIST, PREMIUM
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Instantiate central ViewModel coordinating our persistent DB + Gemini pipelines
        val viewModel = ViewModelProvider(this)[FinancialViewModel::class.java]
 
        setContent {
            MyApplicationTheme(darkTheme = isDarkThemeState, dynamicColor = false) {
                var currentTab by remember { mutableStateOf(DashboardTab.LANDING) }
                val configuration = LocalConfiguration.current
                val isExpanded = configuration.screenWidthDp >= 600
 
                if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CanvasBg)
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        NavigationSidebar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                        VerticalDivider(color = BorderColor)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(CanvasBg)
                        ) {
                            AnimatePresence(targetState = currentTab) { targetTab ->
                                when (targetTab) {
                                    DashboardTab.LANDING -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { LandingScreen(viewModel) }
                                    DashboardTab.RADAR -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { RadarScreen(viewModel) }
                                    DashboardTab.PORTFOLIO -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { PortfolioScreen(viewModel) }
                                    DashboardTab.SCANNER -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { ScannerScreen(viewModel) }
                                    DashboardTab.WATCHLIST -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { WatchlistScreen(viewModel) }
                                    DashboardTab.PREMIUM -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { SubscriptionScreen(viewModel) }
                                }
                            }
                            StockSummarySidePanel(viewModel)
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            val navBarBg = if (isDarkThemeState) Color(0xFF16181D) else Color(0xFFF3F3FA)
                            val selectedIconCol = if (isDarkThemeState) Color(0xFF93C5FD) else Color(0xFF001D36)
                            val selectedTextCol = if (isDarkThemeState) Color(0xFF93C5FD) else Color(0xFF001D36)
                            val unselectedIconCol = if (isDarkThemeState) Color(0xFF94A3B8) else Color(0xFF44474E)
                            val unselectedTextCol = if (isDarkThemeState) Color(0xFF94A3B8) else Color(0xFF44474E)
                            val indicatorCol = if (isDarkThemeState) Color(0xFF1E3A8A) else Color(0xFFD1E4FF)

                            NavigationBar(
                                containerColor = navBarBg,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("dashboard_bottom_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.LANDING,
                                    onClick = { currentTab = DashboardTab.LANDING },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.LANDING) Icons.Filled.Info else Icons.Outlined.Info,
                                            contentDescription = "Welcome"
                                        )
                                    },
                                    label = { Text("Welcome", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_landing")
                                )

                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.RADAR,
                                    onClick = { currentTab = DashboardTab.RADAR },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.RADAR) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Radar"
                                        )
                                    },
                                    label = { Text("AI Radar", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_radar")
                                )

                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.PORTFOLIO,
                                    onClick = { currentTab = DashboardTab.PORTFOLIO },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.PORTFOLIO) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = "Portfolio"
                                        )
                                    },
                                    label = { Text("Portfolio", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_portfolio")
                                )

                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.SCANNER,
                                    onClick = { currentTab = DashboardTab.SCANNER },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.SCANNER) Icons.Filled.Search else Icons.Outlined.Search,
                                            contentDescription = "Scanner"
                                        )
                                    },
                                    label = { Text("Scanners", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_scanner")
                                )

                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.WATCHLIST,
                                    onClick = { currentTab = DashboardTab.WATCHLIST },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.WATCHLIST) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                            contentDescription = "Watchlist"
                                        )
                                    },
                                    label = { Text("Alerts", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_watchlist")
                                )

                                NavigationBarItem(
                                    selected = currentTab == DashboardTab.PREMIUM,
                                    onClick = { currentTab = DashboardTab.PREMIUM },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == DashboardTab.PREMIUM) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Plans"
                                        )
                                    },
                                    label = { Text("Premium", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = selectedIconCol,
                                        selectedTextColor = selectedTextCol,
                                        unselectedIconColor = unselectedIconCol,
                                        unselectedTextColor = unselectedTextCol,
                                        indicatorColor = indicatorCol
                                    ),
                                    modifier = Modifier.testTag("tab_premium")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CanvasBg)
                                .padding(innerPadding)
                        ) {
                            AnimatePresence(targetState = currentTab) { targetTab ->
                                when (targetTab) {
                                    DashboardTab.LANDING -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { LandingScreen(viewModel) }
                                    DashboardTab.RADAR -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { RadarScreen(viewModel) }
                                    DashboardTab.PORTFOLIO -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { PortfolioScreen(viewModel) }
                                    DashboardTab.SCANNER -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { ScannerScreen(viewModel) }
                                    DashboardTab.WATCHLIST -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { WatchlistScreen(viewModel) }
                                    DashboardTab.PREMIUM -> FramerMotionEntrance(modifier = Modifier.fillMaxSize()) { SubscriptionScreen(viewModel) }
                                }
                            }
                            StockSummarySidePanel(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationSidebar(
    currentTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val sidebarBg = PanelBg
    val borderColorVal = BorderColor
    
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(sidebarBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Sharrow Logo",
                        tint = AccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "SHARROW.AI",
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Navigation Options
            val tabs = listOf(
                DashboardTab.LANDING to ("Landing Page" to Icons.Default.Info),
                DashboardTab.RADAR to ("AI Radar" to Icons.Default.Home),
                DashboardTab.PORTFOLIO to ("Portfolio" to Icons.Default.Star),
                DashboardTab.SCANNER to ("Scanners" to Icons.Default.Search),
                DashboardTab.WATCHLIST to ("Alerts" to Icons.Default.Notifications),
                DashboardTab.PREMIUM to ("Premium" to Icons.Default.Settings)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEach { (tab, details) ->
                    val (label, icon) = details
                    val isSelected = currentTab == tab
                    val bg = if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent
                    val contentColor = if (isSelected) AccentBlue else TextGray
                    val tagStr = when(tab) {
                        DashboardTab.LANDING -> "tab_landing_sidebar"
                        DashboardTab.RADAR -> "tab_radar_sidebar"
                        DashboardTab.PORTFOLIO -> "tab_portfolio_sidebar"
                        DashboardTab.SCANNER -> "tab_scanner_sidebar"
                        DashboardTab.WATCHLIST -> "tab_watchlist_sidebar"
                        DashboardTab.PREMIUM -> "tab_premium_sidebar"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { onTabSelected(tab) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag(tagStr),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Footer Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HorizontalDivider(color = borderColorVal.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFD1E4FF), CircleShape)
                            .border(1.dp, Color(0xFF74777F).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SK",
                            color = Color(0xFF001D36),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Sudhakar",
                            color = BrandNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Premium",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                ThemeToggle(tag = "action_toggle_theme_sidebar")
            }
        }
    }
}
