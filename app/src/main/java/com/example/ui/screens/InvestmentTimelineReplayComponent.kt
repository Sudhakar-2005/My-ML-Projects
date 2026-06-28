package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinancialViewModel
import com.example.ui.viewmodel.TimelineReplayItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentTimelineReplayCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val customReplayList by viewModel.customTimelineReplay.collectAsState()
    val isLoadingReplay by viewModel.isLoadingTimelineReplay.collectAsState()
    val holdings by viewModel.holdings.collectAsState()
    val localBorderColor = BorderColor

    var selectedSymbol by remember { mutableStateOf("TCS") }
    var customSymbolInput by remember { mutableStateOf("") }
    var customNameInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Pre-seeded list options
    val defaultStocks = listOf(
        Pair("TCS", "Tata Consultancy Services"),
        Pair("INFY", "Infosys Limited"),
        Pair("RELIANCE", "Reliance Industries"),
        Pair("BEL", "Bharat Electronics Ltd"),
        Pair("AAPL", "Apple Inc."),
        Pair("MSFT", "Microsoft Corp")
    )

    val availableAssets = remember(holdings) {
        val list = defaultStocks.toMutableList()
        holdings.forEach { holding ->
            if (list.none { it.first == holding.symbol }) {
                list.add(Pair(holding.symbol, getStockFullName(holding.symbol)))
            }
        }
        list
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    // local simulation state: nodeId -> isFollowed (true if followed, false if ignored)
    val simulationMap = remember { mutableStateMapOf<String, Boolean>() }

    // Base initial loading
    LaunchedEffect(selectedSymbol) {
        viewModel.generateTimelineReplay(
            selectedSymbol,
            availableAssets.find { it.first == selectedSymbol }?.second ?: getStockFullName(selectedSymbol)
        )
    }

    // Auto seed or reset simulation map when list changes
    LaunchedEffect(customReplayList) {
        simulationMap.clear()
        customReplayList.forEach { item ->
            // Let's seed based on the initial userDecision in the data (contain "Bought" means followed)
            val initialFollow = item.userDecision.contains("Bought", ignoreCase = true) || item.userDecision.contains("Followed", ignoreCase = true)
            simulationMap[item.id] = initialFollow
        }
    }

    GlowCard(
        borderColor = AccentBlue.copy(alpha = 0.35f),
        modifier = modifier
            .testTag("investment_timeline_replay_card")
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header block
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
                        Text("⏳", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "INVESTMENT TIMELINE REPLAY",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Map historical recommendations against actual market outcomes",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector UI Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset Dropdown
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BorderColor.copy(alpha = 0.15f))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Select Asset", fontSize = 9.sp, color = TextGray)
                            Text(
                                text = selectedSymbol,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentTextColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select asset",
                            tint = TextGray
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(PanelBg)
                    ) {
                        availableAssets.forEach { (sym, name) ->
                            DropdownMenuItem(
                                text = { Text("$sym - $name", color = ContentTextColor, fontSize = 12.sp) },
                                onClick = {
                                    selectedSymbol = sym
                                    customSymbolInput = ""
                                    customNameInput = ""
                                    isDropdownExpanded = false
                                    viewModel.generateTimelineReplay(sym, name)
                                }
                            )
                        }
                    }
                }

                // Custom Ticker Field
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = customSymbolInput,
                        onValueChange = { customSymbolInput = it.uppercase() },
                        placeholder = { Text("Or custom ticker...", fontSize = 12.sp, color = TextGray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ContentTextColor,
                            unfocusedTextColor = ContentTextColor,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("timeline_replay_ticker_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Submit Action
                IconButton(
                    onClick = {
                        val sym = if (customSymbolInput.trim().isNotEmpty()) customSymbolInput.trim() else selectedSymbol
                        val name = if (customSymbolInput.trim().isNotEmpty() && customNameInput.trim().isNotEmpty()) {
                            customNameInput.trim()
                        } else {
                            availableAssets.find { it.first == sym }?.second ?: getStockFullName(sym)
                        }
                        viewModel.generateTimelineReplay(sym, name)
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue, RoundedCornerShape(8.dp))
                        .testTag("timeline_replay_generate_button"),
                    enabled = !isLoadingReplay
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Run Timeline Replay",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Company Name Field for custom ticker input
            if (customSymbolInput.trim().isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customNameInput,
                    onValueChange = { customNameInput = it },
                    placeholder = { Text("Enter company full name...", fontSize = 12.sp, color = TextGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (customSymbolInput.trim().isNotEmpty()) {
                                val sym = customSymbolInput.trim()
                                val name = if (customNameInput.trim().isNotEmpty()) customNameInput.trim() else getStockFullName(sym)
                                viewModel.generateTimelineReplay(sym, name)
                                keyboardController?.hide()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentTextColor,
                        unfocusedTextColor = ContentTextColor,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("timeline_replay_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading / Output Render Area
            if (isLoadingReplay) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Aggregating historical milestones & calculating recommendation outcomes...",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("timeline_loading_indicator")
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = customReplayList.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section summary stats (Dynamic Simulation Impact)
                        val principalSum = 100000.0 // Assume ₹1,00,000 baseline investment at each node if followed
                        var totalSimulatedProfit = 0.0
                        var totalPotentialProfit = 0.0 // total possible gains if they followed all
                        var decisionsFollowed = 0
                        var decisionsIgnored = 0

                        customReplayList.forEach { node ->
                            val followed = simulationMap[node.id] ?: false
                            val percentage = node.outcomeGainsPercent
                            val nodeProfit = principalSum * (percentage / 100.0)

                            totalPotentialProfit += nodeProfit
                            if (followed) {
                                totalSimulatedProfit += nodeProfit
                                decisionsFollowed++
                            } else {
                                decisionsIgnored++
                            }
                        }

                        val missedOpportunityCost = totalPotentialProfit - totalSimulatedProfit

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PanelBg)
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                                .testTag("sim_stats_box")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SIMULATED REPLAY PORTFOLIO IMPACT (₹1L PRINCIPAL PER NODE)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray,
                                    letterSpacing = 0.5.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Simulated Gains
                                    Column(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .background(AccentBlue.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("SIMULATED ALPHA", fontSize = 8.sp, color = TextGray)
                                        Text(
                                            text = "₹${String.format("%,.0f", totalSimulatedProfit)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (totalSimulatedProfit >= 0) AccentMint else AccentOrange
                                        )
                                    }

                                    // Opportunity Cost
                                    Column(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .background(AccentOrange.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("OPPORTUNITY COST", fontSize = 8.sp, color = TextGray)
                                        Text(
                                            text = "₹${String.format("%,.0f", missedOpportunityCost)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AccentOrange
                                        )
                                    }

                                    // Followed ratio
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("DECISIONS", fontSize = 8.sp, color = TextGray)
                                        Text(
                                            text = "$decisionsFollowed F / $decisionsIgnored I",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ContentTextColor
                                        )
                                    }
                                }

                                // Informative feedback sentence based on toggles
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Analysis",
                                        tint = AccentMint,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = when {
                                            decisionsFollowed == customReplayList.size -> "Flawless compliance! You've captured 100% of the possible AI Alpha gains."
                                            decisionsFollowed > decisionsIgnored -> "Strong performance! Your choices generated solid compound gains, beating standard indexes."
                                            decisionsFollowed > 0 -> "Moderate efficiency. Toggling more nodes to 'Follow Advice' demonstrates how portfolio alpha compounds."
                                            else -> "Warning: Total non-compliance missed ₹${String.format("%,.0f", missedOpportunityCost)} in cumulative momentum expansion."
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ContentTextColor
                                    )
                                }
                            }
                        }

                        // Chronological Timeline view
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timeline_nodes_container")
                        ) {
                            customReplayList.forEachIndexed { index, node ->
                                val followed = simulationMap[node.id] ?: false

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Left vertical connector line + dot drawing
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(20.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    if (followed) AccentMint else AccentBlue.copy(alpha = 0.4f),
                                                    CircleShape
                                                )
                                                .border(2.dp, Color.White, CircleShape)
                                        )

                                        if (index < customReplayList.size - 1) {
                                            Canvas(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(210.dp)
                                            ) {
                                                drawRect(
                                                    color = localBorderColor.copy(alpha = 0.6f),
                                                    size = size
                                                )
                                            }
                                        }
                                    }

                                    // Content Card
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Header node row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = node.dateStr.uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue,
                                                letterSpacing = 0.5.sp
                                            )

                                            // Replay Toggle button
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BorderColor.copy(alpha = 0.2f))
                                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                                    .height(24.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .background(if (followed) AccentMint else Color.Transparent)
                                                        .clickable { simulationMap[node.id] = true }
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "FOLLOW",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (followed) Color.White else TextGray
                                                    )
                                                }
                                                Divider(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(1.dp),
                                                    color = BorderColor
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .background(if (!followed) AccentOrange else Color.Transparent)
                                                        .clickable { simulationMap[node.id] = false }
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "IGNORE",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (!followed) Color.White else TextGray
                                                    )
                                                }
                                            }
                                        }

                                        // AI Suggestion Box
                                        Text(
                                            text = node.aiSuggestion,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ContentTextColor
                                        )

                                        // Simulation Outcome Panel
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (followed) AccentMint.copy(alpha = 0.08f)
                                                    else AccentOrange.copy(alpha = 0.08f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (followed) AccentMint.copy(alpha = 0.3f)
                                                    else AccentOrange.copy(alpha = 0.3f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (followed) Icons.Default.CheckCircle else Icons.Default.Info,
                                                        contentDescription = "Outcome Status",
                                                        tint = if (followed) AccentMint else AccentOrange,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = if (followed) "SIMULATED POSITION SUCCESS" else "MISSED OPPORTUNITY WARNING",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (followed) AccentMint else AccentOrange
                                                    )
                                                }

                                                val nodeProfit = principalSum * (node.outcomeGainsPercent / 100.0)
                                                Text(
                                                    text = if (followed) {
                                                        "By following AI advice, you captured +${node.outcomeGainsPercent}% asset expansion. Your ₹1,00,000 capital grew to ₹${String.format("%,.0f", principalSum + nodeProfit)} (+₹${String.format("%,.0f", nodeProfit)} profit)."
                                                    } else {
                                                        "By ignoring AI advice, you missed +${node.outcomeGainsPercent}% compound rally. Your principal remained flat, incurring ₹${String.format("%,.0f", nodeProfit)} in missed opportunity cost."
                                                    },
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp,
                                                    color = ContentTextColor
                                                )
                                            }
                                        }

                                        // Retrospective Lesson Learned
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("💡", fontSize = 11.sp)
                                            Text(
                                                text = node.lessonLearned,
                                                fontSize = 11.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = TextGray,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reset Button
                        TextButton(
                            onClick = {
                                simulationMap.clear()
                                customReplayList.forEach { node ->
                                    val initialFollow = node.userDecision.contains("Bought", ignoreCase = true) || node.userDecision.contains("Followed", ignoreCase = true)
                                    simulationMap[node.id] = initialFollow
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("reset_simulation_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Simulation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
