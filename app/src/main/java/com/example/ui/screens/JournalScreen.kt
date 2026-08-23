package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.Trade
import com.example.ui.JournalPeriodFilter
import com.example.ui.JournalResultFilter
import com.example.ui.TradingJournalViewModel
import com.example.ui.components.TradeCardItem
import com.example.ui.components.formatAmount
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen

@Composable
fun JournalScreen(
    viewModel: TradingJournalViewModel,
    onTradeClick: (Trade) -> Unit,
    onAddTradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val filteredTrades by viewModel.filteredTrades.collectAsState()
    val allSetups by viewModel.allSetups.collectAsState()
    val allTrades by viewModel.allTradesDesc.collectAsState()

    val periodFilter by viewModel.journalPeriodFilter.collectAsState()
    val resultFilter by viewModel.journalResultFilter.collectAsState()
    val setupFilter by viewModel.journalSetupFilter.collectAsState()
    val stockFilter by viewModel.journalStockFilter.collectAsState()
    val searchQuery by viewModel.journalSearchQuery.collectAsState()

    val currency = settings.currencySymbol

    // Distinct stocks for filter
    val distinctStocks = remember(allTrades) {
        allTrades.map { it.stockName }.distinct().sorted()
    }

    var showSetupFilterMenu by remember { mutableStateOf(false) }
    var showStockFilterMenu by remember { mutableStateOf(false) }

    val filteredNetPnL = remember(filteredTrades) {
        filteredTrades.sumOf { it.netPnL }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTradeClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Trade") },
                text = { Text("New Trade", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("journal_add_trade_fab")
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Screen Title
            Text(
                text = "Trade Journal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Live Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.journalSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_search_input"),
                placeholder = { Text("Search stock, setup, notes, emotions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.journalSearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Period Filters Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(JournalPeriodFilter.values()) { period ->
                    val isSelected = periodFilter == period
                    val label = when (period) {
                        JournalPeriodFilter.ALL -> "All Time"
                        JournalPeriodFilter.TODAY -> "Today"
                        JournalPeriodFilter.THIS_WEEK -> "This Week"
                        JournalPeriodFilter.THIS_MONTH -> "This Month"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.journalPeriodFilter.value = period },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Secondary Filters: Win/Loss & Setup & Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Result filters (All / Wins / Losses)
                JournalResultFilter.values().forEach { res ->
                    val isSelected = resultFilter == res
                    val label = when (res) {
                        JournalResultFilter.ALL -> "All Results"
                        JournalResultFilter.WINS -> "Wins"
                        JournalResultFilter.LOSSES -> "Losses"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.journalResultFilter.value = res },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (res == JournalResultFilter.WINS) ProfitGreen.copy(alpha = 0.2f)
                            else if (res == JournalResultFilter.LOSSES) LossRed.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            selectedLabelColor = if (res == JournalResultFilter.WINS) ProfitGreen
                            else if (res == JournalResultFilter.LOSSES) LossRed
                            else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Setup Filter Dropdown
                Box {
                    FilterChip(
                        selected = setupFilter != "ALL",
                        onClick = { showSetupFilterMenu = true },
                        label = { Text(if (setupFilter == "ALL") "Setup" else setupFilter, fontSize = 11.sp, maxLines = 1) }
                    )
                    DropdownMenu(
                        expanded = showSetupFilterMenu,
                        onDismissRequest = { showSetupFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Setups") },
                            onClick = {
                                viewModel.journalSetupFilter.value = "ALL"
                                showSetupFilterMenu = false
                            }
                        )
                        allSetups.forEach { setup ->
                            DropdownMenuItem(
                                text = { Text(setup.name) },
                                onClick = {
                                    viewModel.journalSetupFilter.value = setup.name
                                    showSetupFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Summary of currently filtered trades
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTrades.size} trade${if (filteredTrades.size == 1) "" else "s"} found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Net: ${if (filteredNetPnL >= 0) "+" else ""}$currency${formatAmount(filteredNetPnL)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (filteredNetPnL >= 0) ProfitGreen else LossRed
                )
            }

            // Trades List or Empty State
            if (filteredTrades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (allTrades.isEmpty()) "No trades logged yet" else "No trades matching current filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (allTrades.isEmpty()) "Log your first trade to unlock complete journal statistics and calendar analytics."
                            else "Try clearing filters or search query to see your past trades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTrades, key = { it.id }) { trade ->
                        TradeCardItem(
                            trade = trade,
                            currencySymbol = currency,
                            onClick = { onTradeClick(trade) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }
}
