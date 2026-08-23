package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.Trade
import com.example.ui.components.AddEditTradeDialog
import com.example.ui.components.TradeDetailBottomSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.KoushikTradingTheme

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "nav_tab_home"),
    JOURNAL("Journal", Icons.AutoMirrored.Filled.MenuBook, "nav_tab_journal"),
    ANALYTICS("Analytics", Icons.Default.Analytics, "nav_tab_analytics"),
    PROFILE("Profile", Icons.Default.Person, "nav_tab_profile")
}

@Composable
fun MainScreen(
    viewModel: TradingJournalViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val allSetups by viewModel.allSetups.collectAsState()

    val darkTheme = when (settings.themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Dialog & Sheet States
    var showAddTradeDialog by remember { mutableStateOf(false) }
    var tradeToEdit by remember { mutableStateOf<Trade?>(null) }
    var selectedTradeForDetail by remember { mutableStateOf<Trade?>(null) }

    KoushikTradingTheme(darkTheme = darkTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    NavigationTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Crossfade(
                targetState = selectedTab,
                label = "screen_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onAddTradeClick = {
                            tradeToEdit = null
                            showAddTradeDialog = true
                        }
                    )
                    NavigationTab.JOURNAL -> JournalScreen(
                        viewModel = viewModel,
                        onTradeClick = { trade -> selectedTradeForDetail = trade },
                        onAddTradeClick = {
                            tradeToEdit = null
                            showAddTradeDialog = true
                        }
                    )
                    NavigationTab.ANALYTICS -> AnalyticsScreen(
                        viewModel = viewModel,
                        onTradeClick = { trade -> selectedTradeForDetail = trade }
                    )
                    NavigationTab.PROFILE -> ProfileScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        // Add / Edit Trade Dialog
        if (showAddTradeDialog) {
            AddEditTradeDialog(
                initialTrade = tradeToEdit,
                setups = allSetups,
                currencySymbol = settings.currencySymbol,
                onDismiss = {
                    showAddTradeDialog = false
                    tradeToEdit = null
                },
                onSave = { entity ->
                    if (tradeToEdit != null) {
                        viewModel.updateTrade(entity)
                    } else {
                        viewModel.addTrade(entity)
                    }
                    showAddTradeDialog = false
                    tradeToEdit = null
                },
                onAddCustomSetup = { newSetupName ->
                    viewModel.addSetup(newSetupName)
                }
            )
        }

        // Trade Detail Bottom Sheet
        if (selectedTradeForDetail != null) {
            val trade = selectedTradeForDetail!!
            TradeDetailBottomSheet(
                trade = trade,
                currencySymbol = settings.currencySymbol,
                onDismiss = { selectedTradeForDetail = null },
                onEdit = {
                    selectedTradeForDetail = null
                    tradeToEdit = trade
                    showAddTradeDialog = true
                },
                onDelete = {
                    viewModel.deleteTrade(trade)
                    selectedTradeForDetail = null
                }
            )
        }
    }
}
