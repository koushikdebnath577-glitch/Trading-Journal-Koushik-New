package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingRuleEntity
import com.example.ui.TradingJournalViewModel
import com.example.ui.components.CsvExportHelper
import com.example.ui.components.formatAmount
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen

@Composable
fun ProfileScreen(
    viewModel: TradingJournalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val allRules by viewModel.allRules.collectAsState()
    val allSetups by viewModel.allSetups.collectAsState()
    val allTrades by viewModel.allTradesDesc.collectAsState()
    val filteredTrades by viewModel.filteredTrades.collectAsState()
    val monthlyList by viewModel.monthlyPerformanceList.collectAsState()

    // Capital & Risk inputs
    var startingCapStr by remember(settings) { mutableStateOf(settings.startingCapital.toString()) }
    var defaultRiskStr by remember(settings) { mutableStateOf(settings.defaultRiskAmount.toString()) }
    var maxRiskPercentStr by remember(settings) { mutableStateOf(settings.maxRiskPercentPerTrade.toString()) }
    var maxDailyLossStr by remember(settings) { mutableStateOf(settings.maxDailyLoss.toString()) }
    var maxTradesPerDayStr by remember(settings) { mutableStateOf(settings.maxTradesPerDay.toString()) }

    var isEditingCapital by remember { mutableStateOf(false) }

    // Dialog state for new rule
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newRuleText by remember { mutableStateOf("") }
    var newRuleCategory by remember { mutableStateOf("Risk") }

    // Dialog state for new setup
    var showAddSetupDialog by remember { mutableStateOf(false) }
    var newSetupText by remember { mutableStateOf("") }

    // Clear data confirmation
    var showClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Koushik Debnath",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "koushikdebnath577@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Intraday Equity & F&O Trader • ${allTrades.size} Trades Logged",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Section 1: Capital & Risk Management Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAPITAL & RISK PARAMETERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    TextButton(
                        onClick = {
                            if (isEditingCapital) {
                                val sCap = startingCapStr.toDoubleOrNull() ?: settings.startingCapital
                                val defRisk = defaultRiskStr.toDoubleOrNull() ?: settings.defaultRiskAmount
                                val maxRiskPct = maxRiskPercentStr.toDoubleOrNull() ?: settings.maxRiskPercentPerTrade
                                val maxLoss = maxDailyLossStr.toDoubleOrNull() ?: settings.maxDailyLoss
                                val maxTrades = maxTradesPerDayStr.toIntOrNull() ?: settings.maxTradesPerDay

                                viewModel.updateSettings(
                                    settings.copy(
                                        startingCapital = sCap,
                                        defaultRiskAmount = defRisk,
                                        maxRiskPercentPerTrade = maxRiskPct,
                                        maxDailyLoss = maxLoss,
                                        maxTradesPerDay = maxTrades
                                    )
                                )
                                Toast.makeText(context, "Risk parameters updated", Toast.LENGTH_SHORT).show()
                                isEditingCapital = false
                            } else {
                                isEditingCapital = true
                            }
                        },
                        modifier = Modifier.testTag("edit_capital_button")
                    ) {
                        Text(if (isEditingCapital) "Save" else "Edit")
                    }
                }

                if (isEditingCapital) {
                    OutlinedTextField(
                        value = startingCapStr,
                        onValueChange = { startingCapStr = it },
                        label = { Text("Starting Capital (${settings.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultRiskStr,
                        onValueChange = { defaultRiskStr = it },
                        label = { Text("Default Risk Amount (${settings.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = maxDailyLossStr,
                            onValueChange = { maxDailyLossStr = it },
                            label = { Text("Max Daily Loss") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxTradesPerDayStr,
                            onValueChange = { maxTradesPerDayStr = it },
                            label = { Text("Max Daily Trades") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Starting Capital", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${settings.currencySymbol}${formatAmount(settings.startingCapital)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Default Risk / Trade", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${settings.currencySymbol}${formatAmount(settings.defaultRiskAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Daily Loss", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${settings.currencySymbol}${formatAmount(settings.maxDailyLoss)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LossRed)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Max Trades Per Day", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${settings.maxTradesPerDay} trades", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Risk %", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${settings.maxRiskPercentPerTrade}%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section 2: Trading Discipline & Rules Management
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRADING RULES & DISCIPLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(onClick = { showAddRuleDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                allRules.forEach { rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rule.isActive,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleRule(rule, isChecked)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = rule.ruleText,
                                    fontSize = 12.sp,
                                    fontWeight = if (rule.isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (rule.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = rule.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteRule(rule) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = LossRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 3: Trading Setups Manager
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED TRADING SETUPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(onClick = { showAddSetupDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Setup", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                allSetups.forEach { setup ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = setup.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = { viewModel.deleteSetup(setup) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = LossRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 4: Data Export & Backup (CSV)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DATA EXPORT (OFFLINE CSV)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Export your trades and monthly performance to CSV format for use in Google Sheets or Microsoft Excel.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (allTrades.isEmpty()) {
                                Toast.makeText(context, "No trades to export", Toast.LENGTH_SHORT).show()
                            } else {
                                CsvExportHelper.exportTradesToCsv(context, allTrades, settings.currencySymbol, "All_Trades_Journal")
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("export_all_trades_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Trades", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (monthlyList.isEmpty()) {
                                Toast.makeText(context, "No monthly records to export", Toast.LENGTH_SHORT).show()
                            } else {
                                CsvExportHelper.exportMonthlySummaryToCsv(context, monthlyList, settings.currencySymbol)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("export_monthly_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Monthly P&L", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 5: Theme & Customization
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
                    text = "THEME & CURRENCY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                // Theme Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SYSTEM" to "Auto", "DARK" to "Dark", "LIGHT" to "Light").forEach { (mode, label) ->
                            val isSelected = settings.themeMode.equals(mode, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Currency Symbol Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Currency", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("₹" to "₹ INR", "$" to "$ USD", "€" to "€ EUR", "£" to "£ GBP").forEach { (sym, label) ->
                            val isSelected = settings.currencySymbol == sym
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSettings(settings.copy(currencySymbol = sym)) },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Danger Zone
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DANGER ZONE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LossRed,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Reset all journal trade entries and restart performance analytics from clean slate.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { showClearDataDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                    modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Trades Data")
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Add Rule Dialog
    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Add Trading Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRuleText,
                        onValueChange = { newRuleText = it },
                        label = { Text("Rule description") },
                        placeholder = { Text("e.g. Always place stop loss before entering") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRuleCategory,
                        onValueChange = { newRuleCategory = it },
                        label = { Text("Category") },
                        placeholder = { Text("Risk, Execution, Psychology") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRuleText.isNotBlank()) {
                            viewModel.addRule(
                                TradingRuleEntity(
                                    ruleText = newRuleText.trim(),
                                    category = newRuleCategory.trim(),
                                    isActive = true
                                )
                            )
                            newRuleText = ""
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text("Add Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Setup Dialog
    if (showAddSetupDialog) {
        AlertDialog(
            onDismissRequest = { showAddSetupDialog = false },
            title = { Text("Add Trading Setup") },
            text = {
                OutlinedTextField(
                    value = newSetupText,
                    onValueChange = { newSetupText = it },
                    label = { Text("Setup Name") },
                    placeholder = { Text("e.g. Volume Breakout, Double Bottom") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSetupText.isNotBlank()) {
                            viewModel.addSetup(newSetupText.trim())
                            newSetupText = ""
                            showAddSetupDialog = false
                        }
                    }
                ) {
                    Text("Add Setup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSetupDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Reset All Trade Data?") },
            text = { Text("This will permanently delete all logged trades, calculations, and performance records from your device's local database. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllTrades()
                        showClearDataDialog = false
                        Toast.makeText(context, "All trade history cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("Yes, Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }
}
