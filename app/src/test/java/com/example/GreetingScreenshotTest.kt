package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.Trade
import com.example.domain.TradeDirection
import com.example.domain.TradeResult
import com.example.ui.components.PrimaryCapitalCard
import com.example.ui.components.TradeCardItem
import com.example.ui.theme.KoushikTradingTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tradeCard_screenshot() {
        val sampleTrade = Trade(
            id = 1,
            date = "2026-03-01",
            stockName = "NIFTY 22500 CE",
            direction = TradeDirection.LONG,
            entryPrice = 120.0,
            exitPrice = 165.0,
            quantity = 150,
            stopLoss = 105.0,
            target = 180.0,
            charges = 60.0,
            setup = "Morning Breakout",
            mistakes = emptyList(),
            emotion = "Disciplined",
            notes = "Clean 15m breakout above PDH with strong volume",
            grossPnL = 6750.0,
            netPnL = 6690.0,
            riskAmount = 2250.0,
            rMultiple = 2.97,
            result = TradeResult.WIN
        )

        composeTestRule.setContent {
            KoushikTradingTheme {
                TradeCardItem(
                    trade = sampleTrade,
                    currencySymbol = "₹",
                    onClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/trade_card.png")
    }
}
