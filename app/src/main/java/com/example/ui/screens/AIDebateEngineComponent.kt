package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinancialViewModel
import com.example.ui.viewmodel.DebateReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDebateEngineCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val debateReport by viewModel.debateReport.collectAsState()
    val isLoadingDebate by viewModel.isLoadingDebate.collectAsState()
    val holdings by viewModel.holdings.collectAsState()

    var selectedSymbol by remember { mutableStateOf("BEL") }
    var customSymbolInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Aggregate unique symbols for selection (default ones + user's portfolio holdings)
    val availableSymbols = remember(holdings) {
        val list = mutableListOf("BEL", "TCS", "INFY", "RELIANCE")
        holdings.forEach { holding ->
            if (!list.contains(holding.symbol)) {
                list.add(holding.symbol)
            }
        }
        list
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    GlowCard(
        borderColor = AccentBlue.copy(alpha = 0.3f),
        modifier = modifier
            .testTag("ai_debate_engine_card")
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
                        Text("⚖️", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "AI DEBATE ENGINE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Gemini-powered institutional dual-perspective debates",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector row (Dropdown & Custom Input)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Selector
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
                            contentDescription = "Select stock",
                            tint = TextGray
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(PanelBg)
                    ) {
                        availableSymbols.forEach { sym ->
                            DropdownMenuItem(
                                text = { Text(sym, color = ContentTextColor, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedSymbol = sym
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom Ticker Field
                OutlinedTextField(
                    value = customSymbolInput,
                    onValueChange = { customSymbolInput = it.uppercase() },
                    placeholder = { Text("Or enter ticker...", fontSize = 12.sp, color = TextGray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (customSymbolInput.trim().isNotEmpty()) {
                                selectedSymbol = customSymbolInput.trim()
                                viewModel.runStockDebate(selectedSymbol, getStockFullName(selectedSymbol))
                                customSymbolInput = ""
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
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("custom_debate_ticker_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Trigger Button
                IconButton(
                    onClick = {
                        val finalSymbol = if (customSymbolInput.trim().isNotEmpty()) {
                            val temp = customSymbolInput.trim()
                            selectedSymbol = temp
                            customSymbolInput = ""
                            temp
                        } else {
                            selectedSymbol
                        }
                        viewModel.runStockDebate(finalSymbol, getStockFullName(finalSymbol))
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue, RoundedCornerShape(8.dp))
                        .testTag("run_debate_button"),
                    enabled = !isLoadingDebate
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Run Debate",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading / Result View
            if (isLoadingDebate) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
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
                        text = "Gemini convening institutional debate panel for $selectedSymbol...",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val reportToRender = debateReport ?: run {
                    // Trigger initial debate if none loaded
                    LaunchedEffect(selectedSymbol) {
                        viewModel.runStockDebate(selectedSymbol, getStockFullName(selectedSymbol))
                    }
                    null
                }

                reportToRender?.let { report ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DEBATE FOR: ${report.symbol} (${report.companyName})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.testTag("debate_target_header")
                            )

                            // Cases Comparison (responsive / adaptive)
                            val isDark = isDarkThemeState
                            
                            // Bull Case Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF0F3A20) else Color(0xFFE8FDF0))
                                    .border(1.dp, if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .testTag("debate_bull_card")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32), CircleShape)
                                        )
                                        Text(
                                            text = "BULL CASE (BUY SIDE)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                        )
                                    }
                                    Text(
                                        text = report.bullCase,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                                    )
                                }
                            }

                            // Bear Case Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF4A1212) else Color(0xFFFDF2F2))
                                    .border(1.dp, if (isDark) Color(0xFFB71C1C) else Color(0xFFFFCDD2), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .testTag("debate_bear_card")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isDark) Color(0xFFE57373) else Color(0xFFC62828), CircleShape)
                                        )
                                        Text(
                                            text = "BEAR CASE (SHORT SIDE)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
                                        )
                                    }
                                    Text(
                                        text = report.bearCase,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                                    )
                                }
                            }

                            // Neutral Case Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BorderColor.copy(alpha = 0.2f))
                                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .testTag("debate_neutral_card")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(TextGray, CircleShape)
                                        )
                                        Text(
                                            text = "NEUTRAL CASE (TACTICAL HOLD)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextGray
                                        )
                                    }
                                    Text(
                                        text = report.neutralCase,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = ContentTextColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Concluding Verdict Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentBlue.copy(alpha = 0.06f))
                                    .border(1.dp, AccentBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                                    .testTag("debate_verdict_section")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🧠", fontSize = 14.sp)
                                            Text(
                                                text = "SHARROW AI VERDICT",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        // Confidence Score Chip
                                        Box(
                                            modifier = Modifier
                                                .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Conviction: ${report.confidenceScore}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = report.aiVerdict.uppercase(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when {
                                                report.aiVerdict.contains("BUY", ignoreCase = true) || report.aiVerdict.contains("ACCUMULATE", ignoreCase = true) -> AccentMint
                                                report.aiVerdict.contains("SELL", ignoreCase = true) || report.aiVerdict.contains("REDUCE", ignoreCase = true) || report.aiVerdict.contains("AVOID", ignoreCase = true) -> AccentOrange
                                                else -> AccentBlue
                                            },
                                            modifier = Modifier.testTag("debate_verdict_text")
                                        )
                                    }

                                    // Confidence progress bar
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        LinearProgressIndicator(
                                            progress = { report.confidenceScore.toFloat() / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = AccentBlue,
                                            trackColor = BorderColor.copy(alpha = 0.3f)
                                        )
                                    }

                                    Text(
                                        text = report.reasoning,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = ContentTextColor,
                                        fontWeight = FontWeight.Medium
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
