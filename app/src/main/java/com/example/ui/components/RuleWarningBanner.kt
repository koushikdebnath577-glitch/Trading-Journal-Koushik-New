package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TodayWarningState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LossRed

@Composable
fun RuleWarningBanner(
    warningState: TodayWarningState,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    if (!warningState.isMaxTradesWarning && !warningState.isMaxLossWarning) return

    val isLossWarning = warningState.isMaxLossWarning
    val bannerColor = if (isLossWarning) LossRed else AccentGold

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bannerColor.copy(alpha = 0.12f))
            .border(1.dp, bannerColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("rule_warning_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = bannerColor,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLossWarning) "RISK LIMIT BREACHED" else "DAILY TRADE LIMIT REACHED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = bannerColor,
                    letterSpacing = 0.5.sp
                )

                val message = buildString {
                    if (isLossWarning) {
                        append("Today's loss is $currencySymbol${formatAmount(warningState.todayNetPnL)}, exceeding your max daily loss rule ($currencySymbol${formatAmount(warningState.maxDailyLoss)}). Stop trading for today to protect capital.")
                    } else {
                        append("You have taken ${warningState.todayTradeCount} trades today (Max allowed: ${warningState.maxTradesPerDay}). Avoid overtrading and preserve your edge.")
                    }
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }
    }
}
