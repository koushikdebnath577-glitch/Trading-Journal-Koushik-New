package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DailyPerformance
import com.example.domain.Trade
import com.example.ui.TradingJournalViewModel
import com.example.ui.components.DayDetailsDialog
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.TradingCalendarView
import com.example.ui.components.formatAmount
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BreakevenGray
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    viewModel: TradingJournalViewModel,
    onTradeClick: (Trade) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val cumulativePoints by viewModel.cumulativePoints.collectAsState()
    val dailyPerformanceList by viewModel.dailyPerformanceList.collectAsState()
    val weeklyPerformanceList by viewModel.weeklyPerformanceList.collectAsState()
    val monthlyPerformanceList by viewModel.monthlyPerformanceList.collectAsState()
    val mistakeAnalysis by viewModel.mistakeAnalysis.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val currentYearMonth by viewModel.calendarYearMonth.collectAsState()

    val currency = settings.currencySymbol

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Calendar", "Equity Curve", "Setups", "Mistakes", "Periods")

    // State for Day Detail Dialog from calendar
    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var selectedCalendarPerf by remember { mutableStateOf<DailyPerformance?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Title
        Text(
            text = "Performance Analytics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            containerColor = Color.Transparent
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTabIndex) {
                // Tab 0: Monthly Calendar View
                0 -> {
                    TradingCalendarView(
                        currentYearMonth = currentYearMonth,
                        dailyPerformanceList = dailyPerformanceList,
                        currencySymbol = currency,
                        onMonthChange = { newMonth -> viewModel.setCalendarMonth(newMonth) },
                        onDateClick = { date, perf ->
                            selectedCalendarDate = date
                            selectedCalendarPerf = perf
                        }
                    )

                    // Calendar Legend & Explanation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CALENDAR LEGEND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(ProfitGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Profit Day", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(LossRed))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Loss Day", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(BreakevenGray))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Breakeven", fontSize = 11.sp)
                                }
                            }
                            Text(
                                text = "Tap any date to inspect the complete day summary and list of trades executed on that date.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Tab 1: Equity Curve & Drawdowns
                1 -> {
                    EquityCurveChart(
                        points = cumulativePoints,
                        currencySymbol = currency
                    )

                    // Drawdown deep dive
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "DRAWDOWN & CAPITAL RECOVERY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Current Drawdown", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$currency${formatAmount(dashboardMetrics.currentDrawdown)} (${String.format("%.1f", dashboardMetrics.currentDrawdownPercent)}%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (dashboardMetrics.currentDrawdown > 0) LossRed else ProfitGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Max Drawdown Ever", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "$currency${formatAmount(dashboardMetrics.maxDrawdown)} (${String.format("%.1f", dashboardMetrics.maxDrawdownPercent)}%)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (dashboardMetrics.maxDrawdown > 0) LossRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab 2: Setup Analysis
                2 -> {
                    Text(
                        text = "SETUP-WISE PERFORMANCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    val setups = dashboardMetrics.setupPerformanceList
                    if (setups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No trades recorded yet to calculate setup stats", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        setups.forEach { setup ->
                            val isProfitable = setup.totalNetPnL >= 0
                            val setupColor = if (isProfitable) ProfitGreen else LossRed

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = setup.setupName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (isProfitable) "+" else ""}$currency${formatAmount(setup.totalNetPnL)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = setupColor
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Win Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${String.format("%.1f", setup.winRate)}%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (setup.winRate >= 50) ProfitGreen else LossRed
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Trades (W/L)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${setup.totalTrades} (W:${setup.winningTrades} L:${setup.losingTrades})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Avg Win R", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("+${String.format("%.2f", setup.averageWinR)}R", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Expectancy", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${if (setup.expectancy >= 0) "+" else ""}${String.format("%.2f", setup.expectancy)}R", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (setup.expectancy >= 0) ProfitGreen else LossRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 3: Mistake Analysis
                3 -> {
                    Text(
                        text = "PSYCHOLOGY & MISTAKE AUDIT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "MISTAKE IMPACT SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Most Common Mistake", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = mistakeAnalysis.mostCommonMistake,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (mistakeAnalysis.mostCommonMistake == "None") ProfitGreen else LossRed
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Biggest Loss Cause", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = mistakeAnalysis.biggestLossMistake,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (mistakeAnalysis.biggestLossMistake == "None") ProfitGreen else LossRed
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Win Rate (Clean Trades)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format("%.1f", mistakeAnalysis.winRateWithoutMistakes)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Win Rate (Mistake Trades)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format("%.1f", mistakeAnalysis.winRateWithMistakes)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LossRed)
                                }
                            }
                        }
                    }

                    // Mistake Breakdown List
                    if (mistakeAnalysis.mistakeBreakdown.isNotEmpty()) {
                        Text(
                            text = "MISTAKE FREQUENCY & DAMAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        mistakeAnalysis.mistakeBreakdown.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "⚠️ ${item.mistakeName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "Occurred in ${item.count} trade${if (item.count > 1) "s" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Loss: $currency${formatAmount(item.totalLossPnL)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LossRed
                                    )
                                    Text(
                                        text = "Net: ${if (item.netPnL >= 0) "+" else ""}$currency${formatAmount(item.netPnL)}",
                                        fontSize = 11.sp,
                                        color = if (item.netPnL >= 0) ProfitGreen else LossRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab 4: Periodic Breakdowns (Daily, Weekly, Monthly)
                4 -> {
                    // Monthly Breakdown
                    Text(
                        text = "MONTHLY PERFORMANCE HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    if (monthlyPerformanceList.isEmpty()) {
                        Text("No monthly data yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        monthlyPerformanceList.forEach { month ->
                            val isProfitable = month.netPnL >= 0
                            val monthColor = if (isProfitable) ProfitGreen else LossRed

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = month.displayMonth, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        Text(
                                            text = "${if (isProfitable) "+" else ""}$currency${formatAmount(month.netPnL)} (${if (month.netProfitPercent >= 0) "+" else ""}${String.format("%.1f", month.netProfitPercent)}%)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = monthColor
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Win Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format("%.1f", month.winRate)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (month.winRate >= 50) ProfitGreen else LossRed)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Trades", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${month.totalTrades} (W:${month.winningTrades} L:${month.losingTrades})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Avg Win R", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("+${String.format("%.2f", month.averageWinR)}R", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Max Drawdown", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$currency${formatAmount(month.maxDrawdown)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (month.maxDrawdown > 0) LossRed else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weekly Breakdown
                    Text(
                        text = "WEEKLY PERFORMANCE HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    weeklyPerformanceList.take(6).forEach { week ->
                        val isProfitable = week.netPnL >= 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = week.weekLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "${week.totalTrades} trades • Win Rate: ${String.format("%.1f", week.winRate)}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isProfitable) "+" else ""}$currency${formatAmount(week.netPnL)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = if (isProfitable) ProfitGreen else LossRed
                                )
                                Text(
                                    text = "Avg: ${if (week.averageR >= 0) "+" else ""}${String.format("%.2f", week.averageR)}R",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Day Details Dialog if calendar date selected
    if (selectedCalendarDate != null) {
        DayDetailsDialog(
            date = selectedCalendarDate ?: "",
            performance = selectedCalendarPerf,
            currencySymbol = currency,
            onDismiss = {
                selectedCalendarDate = null
                selectedCalendarPerf = null
            },
            onTradeClick = { trade ->
                selectedCalendarDate = null
                selectedCalendarPerf = null
                onTradeClick(trade)
            }
        )
    }
}
