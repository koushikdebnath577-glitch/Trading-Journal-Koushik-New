package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TradeEntity
import com.example.data.model.TradingSetupEntity
import com.example.domain.Trade
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

val DEFAULT_EMOTIONS = listOf(
    "Calm", "Fear", "Greed", "FOMO", "Angry", "Overconfident", "Revenge Trading", "Neutral"
)

val DEFAULT_MISTAKES = listOf(
    "No Mistake",
    "FOMO",
    "Early Entry",
    "Late Entry",
    "Overtrading",
    "Revenge Trading",
    "Moved Stop Loss",
    "Did Not Follow Setup",
    "Emotional Trading",
    "Early Exit",
    "Ignored Stop Loss"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTradeDialog(
    initialTrade: Trade? = null,
    setups: List<TradingSetupEntity>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onSave: (TradeEntity) -> Unit,
    onAddCustomSetup: (String) -> Unit
) {
    val isEdit = initialTrade != null
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    var date by remember { mutableStateOf(initialTrade?.date ?: todayDate) }
    var entryTime by remember { mutableStateOf(initialTrade?.entryTime ?: currentTime) }
    var exitTime by remember { mutableStateOf(initialTrade?.exitTime ?: currentTime) }
    var stockName by remember { mutableStateOf(initialTrade?.stockName ?: "") }
    var direction by remember { mutableStateOf(initialTrade?.direction ?: "BUY") }
    var selectedSetup by remember {
        mutableStateOf(initialTrade?.setup ?: setups.firstOrNull()?.name ?: "Morning Breakout")
    }

    var entryPriceStr by remember { mutableStateOf(initialTrade?.let { it.entryPrice.toString() } ?: "") }
    var exitPriceStr by remember { mutableStateOf(initialTrade?.let { it.exitPrice.toString() } ?: "") }
    var stopLossStr by remember { mutableStateOf(initialTrade?.let { it.stopLoss.toString() } ?: "") }
    var targetPriceStr by remember {
        mutableStateOf(initialTrade?.let { if (it.targetPrice > 0) it.targetPrice.toString() else "" } ?: "")
    }
    var quantityStr by remember { mutableStateOf(initialTrade?.let { it.quantity.toString() } ?: "1") }
    var chargesStr by remember { mutableStateOf(initialTrade?.let { it.charges.toString() } ?: "40.0") }

    var selectedEmotion by remember { mutableStateOf(initialTrade?.emotion ?: "Calm") }
    var selectedMistakes by remember {
        mutableStateOf(
            if (initialTrade != null && initialTrade.mistakes.isNotEmpty()) initialTrade.mistakes.toSet()
            else setOf("No Mistake")
        )
    }
    var notes by remember { mutableStateOf(initialTrade?.notes ?: "") }

    var showSetupDropdown by remember { mutableStateOf(false) }
    var showNewSetupDialog by remember { mutableStateOf(false) }
    var newSetupName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time calculation preview
    val entryPrice = entryPriceStr.toDoubleOrNull() ?: 0.0
    val exitPrice = exitPriceStr.toDoubleOrNull() ?: 0.0
    val stopLoss = stopLossStr.toDoubleOrNull() ?: 0.0
    val targetPrice = targetPriceStr.toDoubleOrNull() ?: 0.0
    val quantity = quantityStr.toIntOrNull() ?: 0
    val charges = chargesStr.toDoubleOrNull() ?: 0.0

    val isBuy = direction.equals("BUY", ignoreCase = true) || direction.equals("LONG", ignoreCase = true)

    val grossPnL by remember(direction, entryPrice, exitPrice, quantity) {
        derivedStateOf {
            if (isBuy) (exitPrice - entryPrice) * quantity
            else (entryPrice - exitPrice) * quantity
        }
    }

    val netPnL by remember(grossPnL, charges) {
        derivedStateOf { grossPnL - charges }
    }

    val riskAmount by remember(direction, entryPrice, stopLoss, quantity) {
        derivedStateOf {
            if (isBuy) {
                if (stopLoss > 0 && entryPrice > stopLoss) (entryPrice - stopLoss) * quantity
                else max(1.0, entryPrice * 0.01 * quantity)
            } else {
                if (stopLoss > 0 && stopLoss > entryPrice) (stopLoss - entryPrice) * quantity
                else max(1.0, entryPrice * 0.01 * quantity)
            }
        }
    }

    val rMultiple by remember(netPnL, riskAmount) {
        derivedStateOf {
            if (riskAmount > 0) netPnL / riskAmount else 0.0
        }
    }

    val isWin = netPnL > 0.01
    val isLoss = netPnL < -0.01

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dialog_close_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = if (isEdit) "Edit Trade" else "Add New Trade",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (stockName.isBlank()) {
                                errorMessage = "Please enter stock name"
                                return@Button
                            }
                            if (entryPrice <= 0 || exitPrice <= 0 || quantity <= 0) {
                                errorMessage = "Please enter valid entry price, exit price and quantity"
                                return@Button
                            }

                            val mistakesString = if (selectedMistakes.isEmpty()) "No Mistake"
                            else selectedMistakes.joinToString(",")

                            val entity = TradeEntity(
                                id = initialTrade?.id ?: 0L,
                                date = date.trim(),
                                entryTime = entryTime.trim(),
                                exitTime = exitTime.trim(),
                                stockName = stockName.trim().uppercase(Locale.getDefault()),
                                setup = selectedSetup.trim(),
                                direction = direction,
                                entryPrice = entryPrice,
                                exitPrice = exitPrice,
                                stopLoss = stopLoss,
                                targetPrice = targetPrice,
                                quantity = quantity,
                                charges = charges,
                                manualRiskAmount = null,
                                emotion = selectedEmotion,
                                mistakes = mistakesString,
                                notes = notes.trim(),
                                chartImageUri = initialTrade?.chartImageUri,
                                createdAt = initialTrade?.entity?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(entity)
                        },
                        modifier = Modifier.testTag("dialog_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = if (isEdit) "Update" else "Save Trade", fontWeight = FontWeight.Bold)
                    }
                }

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LossRed.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Text(text = errorMessage ?: "", color = LossRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Real-time Calculation Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CALCULATED NET P&L",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (netPnL >= 0) "+" else ""}$currencySymbol${formatAmount(netPnL)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWin) ProfitGreen else if (isLoss) LossRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Gross: ${if (grossPnL >= 0) "+" else ""}$currencySymbol${formatAmount(grossPnL)} • Charges: $currencySymbol${formatAmount(charges)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "R MULTIPLE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (rMultiple >= 0) "+" else ""}${String.format("%.2f", rMultiple)}R",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (rMultiple >= 0) ProfitGreen else LossRed
                                )
                                Text(
                                    text = "Risk: $currencySymbol${formatAmount(riskAmount)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Section 1: Core Trade Details
                    Text(
                        text = "TRADE INFORMATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Stock Name & Direction
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stockName,
                            onValueChange = { stockName = it },
                            label = { Text("Stock / Instrument") },
                            placeholder = { Text("e.g. RELIANCE, NIFTY 24500 CE") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_stock"),
                            singleLine = true
                        )

                        // Direction Selector
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isBuy) ProfitGreen else Color.Transparent)
                                    .clickable { direction = "BUY" }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "BUY",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBuy) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isBuy) LossRed else Color.Transparent)
                                    .clickable { direction = "SELL" }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "SELL",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isBuy) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Setup Selector & Custom Setup Button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedSetup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trading Setup") },
                            trailingIcon = {
                                IconButton(onClick = { showSetupDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSetupDropdown = true }
                                .testTag("trade_input_setup")
                        )

                        DropdownMenu(
                            expanded = showSetupDropdown,
                            onDismissRequest = { showSetupDropdown = false }
                        ) {
                            setups.forEach { setup ->
                                DropdownMenuItem(
                                    text = { Text(setup.name) },
                                    onClick = {
                                        selectedSetup = setup.name
                                        showSetupDropdown = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Add Custom Setup...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showSetupDropdown = false
                                    showNewSetupDialog = true
                                }
                            )
                        }
                    }

                    // Date & Time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = entryTime,
                            onValueChange = { entryTime = it },
                            label = { Text("Entry Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = exitTime,
                            onValueChange = { exitTime = it },
                            label = { Text("Exit Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Section 2: Numbers & Execution
                    Text(
                        text = "PRICING & EXECUTION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = entryPriceStr,
                            onValueChange = { entryPriceStr = it },
                            label = { Text("Entry Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_entry_price"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = exitPriceStr,
                            onValueChange = { exitPriceStr = it },
                            label = { Text("Exit Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_exit_price"),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stopLossStr,
                            onValueChange = { stopLossStr = it },
                            label = { Text("Stop Loss") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_stop_loss"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = targetPriceStr,
                            onValueChange = { targetPriceStr = it },
                            label = { Text("Target Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_target"),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_quantity"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = chargesStr,
                            onValueChange = { chargesStr = it },
                            label = { Text("Charges ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trade_input_charges"),
                            singleLine = true
                        )
                    }

                    // Section 3: Psychology & Emotions
                    Text(
                        text = "EMOTION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DEFAULT_EMOTIONS.forEach { emotion ->
                            val isSelected = selectedEmotion == emotion
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEmotion = emotion },
                                label = { Text(emotion) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Section 4: Mistakes (Multi-select)
                    Text(
                        text = "MISTAKES (Select all that apply)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DEFAULT_MISTAKES.forEach { mistake ->
                            val isSelected = selectedMistakes.contains(mistake)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val current = selectedMistakes.toMutableSet()
                                    if (mistake == "No Mistake") {
                                        current.clear()
                                        current.add("No Mistake")
                                    } else {
                                        current.remove("No Mistake")
                                        if (isSelected) current.remove(mistake)
                                        else current.add(mistake)
                                        if (current.isEmpty()) current.add("No Mistake")
                                    }
                                    selectedMistakes = current
                                },
                                label = { Text(mistake) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (mistake == "No Mistake") ProfitGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f),
                                    selectedLabelColor = if (mistake == "No Mistake") ProfitGreen else LossRed
                                )
                            )
                        }
                    }

                    // Section 5: Journal Notes
                    Text(
                        text = "JOURNAL NOTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("What did you do well? What can be improved?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("trade_input_notes"),
                        maxLines = 4
                    )
                }
            }
        }
    }

    // New Custom Setup Dialog
    if (showNewSetupDialog) {
        AlertDialog(
            onDismissRequest = { showNewSetupDialog = false },
            title = { Text("Add Custom Setup") },
            text = {
                OutlinedTextField(
                    value = newSetupName,
                    onValueChange = { newSetupName = it },
                    label = { Text("Setup Name") },
                    placeholder = { Text("e.g. Trendline Retest, Cup & Handle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSetupName.isNotBlank()) {
                            onAddCustomSetup(newSetupName.trim())
                            selectedSetup = newSetupName.trim()
                            newSetupName = ""
                            showNewSetupDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSetupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
