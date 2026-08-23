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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DashboardMetrics
import com.example.ui.TodayWarningState
import com.example.ui.TradingJournalViewModel
import com.example.ui.components.CompactMetricItem
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.PrimaryCapitalCard
import com.example.ui.components.RuleWarningBanner
import com.example.ui.components.formatAmount
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BreakevenGray
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen

@Composable
fun DashboardScreen(
    viewModel: TradingJournalViewModel,
    onAddTradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val cumulativePoints by viewModel.cumulativePoints.collectAsState()
    val warningState by viewModel.todayWarningState.collectAsState()

    val currency = settings.currencySymbol

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTradeClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Trade") },
                text = { Text("Add Trade", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("dashboard_add_trade_fab")
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Title & Tagline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Koushik Trading Journal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intraday Trading & Performance Analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OFFLINE ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Primary Capital & Net Return Card
            PrimaryCapitalCard(
                startingCapital = metrics.startingCapital,
                currentCapital = metrics.currentCapital,
                netProfit = metrics.netProfit,
                netProfitPercent = metrics.netProfitPercent,
                currencySymbol = currency
            )

            // Rule Warning Banner (if breached today)
            RuleWarningBanner(warningState = warningState, currencySymbol = currency)

            // Core R-Multiple & Win-Rate Stats Grid
            Text(
                text = "TRADING PERFORMANCE METRICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Row 1: Win Rate & Loss Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricItem(
                    title = "Win Rate",
                    value = "${String.format("%.1f", metrics.winRate)}%",
                    subtitle = "${metrics.winningTrades} of ${metrics.totalTrades} trades",
                    valueColor = ProfitGreen,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    title = "Loss Rate",
                    value = "${String.format("%.1f", metrics.lossRate)}%",
                    subtitle = "${metrics.losingTrades} losses • ${metrics.breakevenTrades} BE",
                    valueColor = LossRed,
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Expectancy & Profit Factor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricItem(
                    title = "Expectancy (R)",
                    value = "${if (metrics.expectancy >= 0) "+" else ""}${String.format("%.2f", metrics.expectancy)}R",
                    subtitle = "Average R: ${String.format("%.2f", metrics.averageR)}R",
                    valueColor = if (metrics.expectancy >= 0) ProfitGreen else LossRed,
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    title = "Profit Factor",
                    value = if (metrics.profitFactor >= 100) "∞" else String.format("%.2f", metrics.profitFactor),
                    subtitle = "Gross Profit / Gross Loss",
                    valueColor = if (metrics.profitFactor >= 1.5) ProfitGreen else MaterialTheme.colorScheme.onSurface,
                    icon = Icons.Default.Percent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Average Win R & Average Loss R
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactMetricItem(
                    title = "Avg Win R",
                    value = "+${String.format("%.2f", metrics.averageWinR)}R",
                    subtitle = "Wins avg return",
                    valueColor = ProfitGreen,
                    modifier = Modifier.weight(1f)
                )
                CompactMetricItem(
                    title = "Avg Loss R",
                    value = "${String.format("%.2f", metrics.averageLossR)}R",
                    subtitle = "Losses avg R",
                    valueColor = LossRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Financial P&L & Charges Breakdown Card
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
                        text = "FINANCIAL BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Total Gross Profit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "+$currency${formatAmount(metrics.totalGrossProfit)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Gross Loss", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$currency${formatAmount(metrics.totalGrossLoss)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LossRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Charges", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$currency${formatAmount(metrics.totalCharges)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                        }
                    }
                }
            }

            // Drawdown Metrics Card
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
                        text = "CAPITAL DRAWDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Current Drawdown", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$currency${formatAmount(metrics.currentDrawdown)} (${String.format("%.1f", metrics.currentDrawdownPercent)}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.currentDrawdown > 0) LossRed else ProfitGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Maximum Drawdown", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$currency${formatAmount(metrics.maxDrawdown)} (${String.format("%.1f", metrics.maxDrawdownPercent)}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.maxDrawdown > 0) LossRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Setup Performance Snapshot
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
                        text = "SETUP PERFORMANCE SNAPSHOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    if (metrics.setupPerformanceList.isEmpty()) {
                        Text(
                            text = "No setup trades yet. Add trades to track setup win rates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        metrics.setupPerformanceList.take(4).forEach { setup ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = setup.setupName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "${setup.totalTrades} trades (W:${setup.winningTrades} L:${setup.losingTrades})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Win Rate: ${String.format("%.1f", setup.winRate)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (setup.winRate >= 50) ProfitGreen else LossRed
                                    )
                                    Text(
                                        text = "Net: ${if (setup.totalNetPnL >= 0) "+" else ""}$currency${formatAmount(setup.totalNetPnL)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (setup.totalNetPnL >= 0) ProfitGreen else LossRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Equity Curve Chart Section
            EquityCurveChart(
                points = cumulativePoints,
                currencySymbol = currency
            )

            // Performance Highlights Card
            val highlights = metrics.highlights
            if (highlights.bestTrade != null || highlights.bestDay != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HIGHLIGHTS & RECORDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )

                        if (highlights.bestTrade != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Best Trade", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${highlights.bestTrade.stockName} (+$currency${formatAmount(highlights.bestTrade.netPnL)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                        }

                        if (highlights.highestRTrade != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Highest R Trade", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${highlights.highestRTrade.stockName} (+${String.format("%.2f", highlights.highestRTrade.rMultiple)}R)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                        }

                        if (highlights.bestDay != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Best Day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${highlights.bestDay.date} (+$currency${formatAmount(highlights.bestDay.netPnL)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                        }

                        if (highlights.bestSetup != "None") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Top Setup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = highlights.bestSetup, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
