package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DailyPerformance
import com.example.domain.Trade
import com.example.ui.theme.BreakevenGray
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBgDark
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBgDark
import com.example.ui.theme.ProfitGreenLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dayNumber: Int,
    val fullDateString: String,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val dailyPerformance: DailyPerformance?
)

@Composable
fun TradingCalendarView(
    currentYearMonth: String, // "YYYY-MM"
    dailyPerformanceList: List<DailyPerformance>,
    currencySymbol: String = "₹",
    onMonthChange: (String) -> Unit,
    onDateClick: (String, DailyPerformance?) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val displayFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val parsedMonth = remember(currentYearMonth) {
        try {
            monthFormat.parse(currentYearMonth) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    val displayMonthTitle = remember(parsedMonth) {
        displayFormat.format(parsedMonth)
    }

    val todayStr = remember { dayDateFormat.format(Date()) }

    // Map of day date string -> DailyPerformance
    val dayMap = remember(dailyPerformanceList) {
        dailyPerformanceList.associateBy { it.date }
    }

    // Monthly total net P&L
    val monthlyTotalPnL = remember(dailyPerformanceList, currentYearMonth) {
        dailyPerformanceList
            .filter { it.date.startsWith(currentYearMonth) }
            .sumOf { it.netPnL }
    }

    val daysInGrid = remember(currentYearMonth, dayMap) {
        val cal = Calendar.getInstance()
        cal.time = parsedMonth
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<CalendarDayItem>()

        // Empty padding cells before first day of month (Sunday is 1)
        for (i in 1 until firstDayOfWeek) {
            list.add(
                CalendarDayItem(
                    dayNumber = 0,
                    fullDateString = "",
                    isCurrentMonth = false,
                    isToday = false,
                    dailyPerformance = null
                )
            )
        }

        // Real days of this month
        for (d in 1..daysInMonth) {
            val dateStr = String.format(Locale.getDefault(), "%s-%02d", currentYearMonth, d)
            val isToday = dateStr == todayStr
            val perf = dayMap[dateStr]

            list.add(
                CalendarDayItem(
                    dayNumber = d,
                    fullDateString = dateStr,
                    isCurrentMonth = true,
                    isToday = isToday,
                    dailyPerformance = perf
                )
            )
        }

        list
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trading_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Header with Prev/Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayMonthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = parsedMonth
                            cal.add(Calendar.MONTH, -1)
                            onMonthChange(monthFormat.format(cal.time))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = parsedMonth
                            cal.add(Calendar.MONTH, 1)
                            onMonthChange(monthFormat.format(cal.time))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Monthly Net P&L Banner
            val isMonthlyProfit = monthlyTotalPnL >= 0
            val pnlColor = if (isMonthlyProfit) ProfitGreen else LossRed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Total Net P&L",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (isMonthlyProfit) "+" else ""}$currencySymbol${formatAmount(monthlyTotalPnL)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pnlColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of Week Header
            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayNames.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Cells
            val rows = daysInGrid.chunked(7)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0..6) {
                            val item = week.getOrNull(i)
                            if (item != null && item.isCurrentMonth) {
                                CalendarCell(
                                    item = item,
                                    currencySymbol = currencySymbol,
                                    onClick = {
                                        onDateClick(item.fullDateString, item.dailyPerformance)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarCell(
    item: CalendarDayItem,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val perf = item.dailyPerformance
    val hasTrades = perf != null && perf.totalTrades > 0
    val isProfit = (perf?.netPnL ?: 0.0) > 0.01
    val isLoss = (perf?.netPnL ?: 0.0) < -0.01

    val cellBg = when {
        isProfit -> ProfitGreen.copy(alpha = 0.18f)
        isLoss -> LossRed.copy(alpha = 0.18f)
        hasTrades -> BreakevenGray.copy(alpha = 0.15f)
        item.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        item.isToday -> MaterialTheme.colorScheme.primary
        isProfit -> ProfitGreen.copy(alpha = 0.5f)
        isLoss -> LossRed.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(0.95f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellBg)
            .border(if (item.isToday || hasTrades) 1.dp else 0.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${item.dayNumber}",
                fontSize = 11.sp,
                fontWeight = if (item.isToday || hasTrades) FontWeight.Bold else FontWeight.Normal,
                color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (hasTrades && perf != null) {
                Spacer(modifier = Modifier.height(2.dp))
                val pnlText = if (perf.netPnL >= 1000 || perf.netPnL <= -1000) {
                    val kVal = perf.netPnL / 1000.0
                    "${if (perf.netPnL >= 0) "+" else ""}${String.format("%.1fk", kVal)}"
                } else {
                    "${if (perf.netPnL >= 0) "+" else ""}${perf.netPnL.toInt()}"
                }

                Text(
                    text = pnlText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) ProfitGreen else if (isLoss) LossRed else BreakevenGray,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
