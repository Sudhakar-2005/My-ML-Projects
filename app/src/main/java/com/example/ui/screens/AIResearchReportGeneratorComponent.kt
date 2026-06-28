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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinancialViewModel
import com.example.ui.viewmodel.ResearchReport
import com.example.utils.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIResearchReportGeneratorCard(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customReport by viewModel.customResearchReport.collectAsState()
    val isLoadingResearch by viewModel.isLoadingResearch.collectAsState()
    val holdings by viewModel.holdings.collectAsState()

    var selectedSymbol by remember { mutableStateOf("TCS") }
    var customSymbolInput by remember { mutableStateOf("") }
    var customNameInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Pre-seed some options
    val defaultStocks = listOf(
        Pair("TCS", "Tata Consultancy Services"),
        Pair("INFY", "Infosys Limited"),
        Pair("RELIANCE", "Reliance Industries"),
        Pair("BEL", "Bharat Electronics Ltd"),
        Pair("AAPL", "Apple Inc."),
        Pair("MSFT", "Microsoft Corp")
    )

    // Merge default options and holdings
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

    GlowCard(
        borderColor = AccentBlue.copy(alpha = 0.35f),
        modifier = modifier
            .testTag("ai_research_report_card")
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
                        Text("📁", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "AI RESEARCH REPORT GENERATOR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Generate institutional grade PDF reports instantly",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selection controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Selector for Preset Stocks
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
                                    // Trigger report generation automatically
                                    viewModel.generateResearchReport(sym, name)
                                }
                            )
                        }
                    }
                }

                // Custom Ticker input fields
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
                            .testTag("custom_research_ticker_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Trigger Button
                IconButton(
                    onClick = {
                        val sym = if (customSymbolInput.trim().isNotEmpty()) customSymbolInput.trim() else selectedSymbol
                        val name = if (customSymbolInput.trim().isNotEmpty() && customNameInput.trim().isNotEmpty()) {
                            customNameInput.trim()
                        } else {
                            availableAssets.find { it.first == sym }?.second ?: getStockFullName(sym)
                        }
                        viewModel.generateResearchReport(sym, name)
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentBlue, RoundedCornerShape(8.dp))
                        .testTag("generate_report_button"),
                    enabled = !isLoadingResearch
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Generate Research Report",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Optional company name field if custom ticker is active
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
                                viewModel.generateResearchReport(sym, name)
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
                        .testTag("custom_research_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading state view
            if (isLoadingResearch) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentBlue,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Assembling SWOT metrics, debt assessments, and financial models...",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("research_loading_indicator")
                        )
                    }
                    ResearchReportSkeleton()
                }
            } else {
                val reportToRender = customReport ?: run {
                    // Trigger initial report on load
                    LaunchedEffect(selectedSymbol) {
                        viewModel.generateResearchReport(selectedSymbol, availableAssets.find { it.first == selectedSymbol }?.second ?: getStockFullName(selectedSymbol))
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Section header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RESEARCH MEMORANDUM: ${report.symbol}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.testTag("research_report_header")
                                )

                                // Download PDF action
                                TextButton(
                                    onClick = { PdfReportGenerator.generateAndSharePdf(context, report) },
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("download_pdf_report_button"),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Export PDF",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 1. Executive overview description box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BorderColor.copy(alpha = 0.12f))
                                    .border(1.dp, BorderColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = report.companyName.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                    Text(
                                        text = report.overview,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = ContentTextColor
                                    )
                                }
                            }

                            // 2. SWOT quadrant visual representation (2x2 Grid)
                            Text(
                                text = "STRATEGIC SWOT RADAR MATRIX",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGray,
                                letterSpacing = 0.5.sp
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val swotKeys = listOf("Strengths", "Weaknesses", "Opportunities", "Threats")
                                
                                swotKeys.chunked(2).forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        row.forEach { category ->
                                            val list = report.swotAnalysis[category] ?: emptyList()
                                            
                                            val categoryColor = when (category) {
                                                "Strengths" -> if (isDarkThemeState) Color(0xFF0F3A20) else Color(0xFFE8FDF0)
                                                "Weaknesses" -> if (isDarkThemeState) Color(0xFF4A1212) else Color(0xFFFDF2F2)
                                                "Opportunities" -> if (isDarkThemeState) Color(0xFF0F2B48) else Color(0xFFEFF6FF)
                                                else -> if (isDarkThemeState) Color(0xFF42330F) else Color(0xFFFEF3C7)
                                            }

                                            val categoryBorder = when (category) {
                                                "Strengths" -> if (isDarkThemeState) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
                                                "Weaknesses" -> if (isDarkThemeState) Color(0xFFB71C1C) else Color(0xFFFFCDD2)
                                                "Opportunities" -> if (isDarkThemeState) Color(0xFF1E40AF) else Color(0xFFBFDBFE)
                                                else -> if (isDarkThemeState) Color(0xFFB45309) else Color(0xFFFDE68A)
                                            }

                                            val textColor = when (category) {
                                                "Strengths" -> if (isDarkThemeState) Color(0xFF81C784) else Color(0xFF2E7D32)
                                                "Weaknesses" -> if (isDarkThemeState) Color(0xFFE57373) else Color(0xFFC62828)
                                                "Opportunities" -> if (isDarkThemeState) Color(0xFF60A5FA) else Color(0xFF1E40AF)
                                                else -> if (isDarkThemeState) Color(0xFFFBBF24) else Color(0xFFB45309)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(categoryColor)
                                                    .border(1.dp, categoryBorder, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                                    .testTag("swot_card_$category")
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = category.uppercase(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor
                                                    )
                                                    
                                                    list.forEach { point ->
                                                        Text(
                                                            text = "• $point",
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp,
                                                            color = ContentTextColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Financial Metrics card (Debt, Valuation, Growth)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PanelBg)
                                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "FINANCIAL RUNWAY & DEBT ASSESSMENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray,
                                        letterSpacing = 0.5.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Revenue Growth
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("REV GROWTH", fontSize = 8.sp, color = TextGray)
                                            Text(
                                                text = report.revenueGrowth,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AccentMint
                                            )
                                        }

                                        // Profit Growth
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("PROFIT GROWTH", fontSize = 8.sp, color = TextGray)
                                            Text(
                                                text = report.profitGrowth,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AccentMint
                                            )
                                        }

                                        // Debt assessment
                                        Column(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("DEBT TO EQUITY", fontSize = 8.sp, color = TextGray)
                                            Text(
                                                text = report.debtEquity,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ContentTextColor,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Investment Thesis Card with Confidence Score
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentBlue.copy(alpha = 0.06f))
                                    .border(1.dp, AccentBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                                    .testTag("research_thesis_section")
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
                                            Text("🔮", fontSize = 14.sp)
                                            Text(
                                                text = "SHARROW INSTITUTIONAL THESIS",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        // Valuation Rating tag
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when {
                                                        report.valuationRating.contains("UNDER", ignoreCase = true) -> AccentMint.copy(alpha = 0.15f)
                                                        report.valuationRating.contains("OVER", ignoreCase = true) -> AccentOrange.copy(alpha = 0.15f)
                                                        else -> AccentBlue.copy(alpha = 0.15f)
                                                    },
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = report.valuationRating.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    report.valuationRating.contains("UNDER", ignoreCase = true) -> AccentMint
                                                    report.valuationRating.contains("OVER", ignoreCase = true) -> AccentOrange
                                                    else -> AccentBlue
                                                }
                                            )
                                        }
                                    }

                                    Text(
                                        text = report.investmentThesis,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = ContentTextColor,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Confidence / conviction meter
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "THESIS CONVICTION RATE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextGray
                                            )
                                            Text(
                                                text = "${report.confidenceScore}% Confidence",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue
                                            )
                                        }

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
                                }
                            }

                            // 5. Peer Standing / Risks Summary
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BorderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(AccentOrange.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Warning",
                                        tint = AccentOrange,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "RISKS NODE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentOrange
                                    )
                                    Text(
                                        text = report.riskAnalysis,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = ContentTextColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "COMPETITIVE PEER BENCHMARK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlue
                                    )
                                    Text(
                                        text = report.peerComparisonText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = ContentTextColor
                                    )
                                }
                            }

                            // Download PDF Core Button at the bottom
                            Button(
                                onClick = { PdfReportGenerator.generateAndSharePdf(context, report) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("core_download_pdf_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Report",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Compile & Export A4 PDF Report",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
