package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CumulativeTradePoint
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class EquityCurveRange {
    ALL_TIME, THIS_MONTH, RECENT_20
}

@Composable
fun EquityCurveChart(
    points: List<CumulativeTradePoint>,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(EquityCurveRange.ALL_TIME) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val currentMonthPrefix = remember {
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    val displayPoints = remember(points, selectedRange) {
        when (selectedRange) {
            EquityCurveRange.ALL_TIME -> points
            EquityCurveRange.THIS_MONTH -> points.filter { it.date.startsWith(currentMonthPrefix) }
            EquityCurveRange.RECENT_20 -> points.takeLast(20)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("equity_curve_card"),
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
            // Header with range chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EQUITY CURVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    val lastCumulative = displayPoints.lastOrNull()?.cumulativeNetPnL ?: 0.0
                    val isProfit = lastCumulative >= 0
                    Text(
                        text = "${if (isProfit) "+" else ""}$currencySymbol${formatAmount(lastCumulative)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitGreen else LossRed
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EquityCurveRange.values().forEach { range ->
                        val isSelected = selectedRange == range
                        val label = when (range) {
                            EquityCurveRange.ALL_TIME -> "All"
                            EquityCurveRange.THIS_MONTH -> "Month"
                            EquityCurveRange.RECENT_20 -> "Last 20"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        selectedRange = range
                                        selectedPointIndex = null
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip if user tapped a point
            val selectedPoint = selectedPointIndex?.let { idx ->
                displayPoints.getOrNull(idx)
            }

            if (selectedPoint != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "#${selectedPoint.tradeIndex} ${selectedPoint.stockName} (${selectedPoint.date})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Trade P&L: ${if (selectedPoint.tradeNetPnL >= 0) "+" else ""}$currencySymbol${formatAmount(selectedPoint.tradeNetPnL)}",
                            fontSize = 11.sp,
                            color = if (selectedPoint.tradeNetPnL >= 0) ProfitGreen else LossRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Cumulative P&L",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${formatAmount(selectedPoint.cumulativeNetPnL)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPoint.cumulativeNetPnL >= 0) ProfitGreen else LossRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Canvas Chart Area
            if (displayPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trades in selected period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val lineColor = if ((displayPoints.lastOrNull()?.cumulativeNetPnL ?: 0.0) >= 0) ProfitGreen else LossRed
                val lineGradient = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.0f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .pointerInput(displayPoints) {
                                detectTapGestures { offset ->
                                    if (displayPoints.isNotEmpty()) {
                                        val stepX = size.width / (displayPoints.size + 1)
                                        val clickedIdx = ((offset.x - stepX) / stepX).toInt()
                                            .coerceIn(0, displayPoints.size - 1)
                                        selectedPointIndex = clickedIdx
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val paddingBottom = 24.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val chartHeight = canvasHeight - paddingBottom - paddingTop

                        val values = displayPoints.map { it.cumulativeNetPnL }
                        val minVal = min(0.0, values.minOrNull() ?: 0.0)
                        val maxVal = max(0.0, values.maxOrNull() ?: 0.0)
                        val rangeVal = if (maxVal - minVal > 0.001) maxVal - minVal else 1.0

                        // Function to map (index, value) to pixel (x, y)
                        val numPoints = displayPoints.size
                        fun getX(idx: Int): Float {
                            if (numPoints <= 1) return canvasWidth / 2f
                            return (idx.toFloat() / (numPoints - 1)) * (canvasWidth - 32.dp.toPx()) + 16.dp.toPx()
                        }

                        fun getY(v: Double): Float {
                            val ratio = (v - minVal) / rangeVal
                            return (canvasHeight - paddingBottom - (ratio * chartHeight)).toFloat()
                        }

                        // Zero baseline
                        val zeroY = getY(0.0)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = Offset(8.dp.toPx(), zeroY),
                            end = Offset(canvasWidth - 8.dp.toPx(), zeroY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Path construction
                        val linePath = Path()
                        val fillPath = Path()

                        val firstX = getX(0)
                        val firstY = getY(displayPoints[0].cumulativeNetPnL)

                        linePath.moveTo(firstX, firstY)
                        fillPath.moveTo(firstX, zeroY)
                        fillPath.lineTo(firstX, firstY)

                        for (i in 1 until displayPoints.size) {
                            val prevX = getX(i - 1)
                            val prevY = getY(displayPoints[i - 1].cumulativeNetPnL)
                            val currX = getX(i)
                            val currY = getY(displayPoints[i].cumulativeNetPnL)

                            // Smooth cubic bezier
                            val cX1 = (prevX + currX) / 2f
                            val cY1 = prevY
                            val cX2 = (prevX + currX) / 2f
                            val cY2 = currY

                            linePath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                            fillPath.cubicTo(cX1, cY1, cX2, cY2, currX, currY)
                        }

                        val lastX = getX(displayPoints.size - 1)
                        fillPath.lineTo(lastX, zeroY)
                        fillPath.close()

                        // Draw Fill
                        drawPath(path = fillPath, brush = lineGradient)

                        // Draw Line
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw Points
                        displayPoints.forEachIndexed { idx, pt ->
                            val ptX = getX(idx)
                            val ptY = getY(pt.cumulativeNetPnL)
                            val isSelected = selectedPointIndex == idx

                            drawCircle(
                                color = if (pt.tradeNetPnL >= 0) ProfitGreen else LossRed,
                                radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                                center = Offset(ptX, ptY)
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = Offset(ptX, ptY)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
