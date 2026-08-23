package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.AnalyticsEngine
import com.example.domain.Trade
import com.example.domain.TradeCalculations
import com.example.domain.TradeDirection
import com.example.domain.TradeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Koushik Trading Journal", appName)
    }

    @Test
    fun `verify Long trade calculation formulas`() {
        // Long Trade: Entry = 100, Exit = 110, Qty = 50, SL = 95, Charges = 20
        // Gross P&L = (110 - 100) * 50 = +500
        // Net P&L = 500 - 20 = +480
        // Risk Amount = (100 - 95) * 50 = 250
        // R Multiple = 480 / 250 = +1.92R
        val gross = TradeCalculations.calculateGrossPnL(TradeDirection.LONG, 100.0, 110.0, 50)
        val net = TradeCalculations.calculateNetPnL(gross, 20.0)
        val risk = TradeCalculations.calculateRiskAmount(TradeDirection.LONG, 100.0, 95.0, 50)
        val rMultiple = TradeCalculations.calculateRMultiple(net, risk)
        val result = TradeCalculations.calculateTradeResult(net)

        assertEquals(500.0, gross, 0.001)
        assertEquals(480.0, net, 0.001)
        assertEquals(250.0, risk, 0.001)
        assertEquals(1.92, rMultiple, 0.001)
        assertEquals(TradeResult.WIN, result)
    }

    @Test
    fun `verify Short trade calculation formulas`() {
        // Short Trade: Entry = 500, Exit = 510, Qty = 20, SL = 505, Charges = 15
        // Gross P&L = (500 - 510) * 20 = -200
        // Net P&L = -200 - 15 = -215
        // Risk Amount = (505 - 500) * 20 = 100
        // R Multiple = -215 / 100 = -2.15R
        val gross = TradeCalculations.calculateGrossPnL(TradeDirection.SHORT, 500.0, 510.0, 20)
        val net = TradeCalculations.calculateNetPnL(gross, 15.0)
        val risk = TradeCalculations.calculateRiskAmount(TradeDirection.SHORT, 500.0, 505.0, 20)
        val rMultiple = TradeCalculations.calculateRMultiple(net, risk)
        val result = TradeCalculations.calculateTradeResult(net)

        assertEquals(-200.0, gross, 0.001)
        assertEquals(-215.0, net, 0.001)
        assertEquals(100.0, risk, 0.001)
        assertEquals(-2.15, rMultiple, 0.001)
        assertEquals(TradeResult.LOSS, result)
    }

    @Test
    fun `verify AnalyticsEngine metrics and win rate`() {
        val trade1 = Trade(
            id = 1,
            date = "2026-03-01",
            stockName = "RELIANCE",
            direction = TradeDirection.LONG,
            entryPrice = 2500.0,
            exitPrice = 2550.0,
            quantity = 10,
            stopLoss = 2480.0,
            target = 2600.0,
            charges = 20.0,
            setup = "Morning Breakout",
            mistakes = emptyList(),
            emotion = "Disciplined",
            notes = "Good breakout",
            grossPnL = 500.0,
            netPnL = 480.0,
            riskAmount = 200.0,
            rMultiple = 2.4,
            result = TradeResult.WIN
        )

        val trade2 = Trade(
            id = 2,
            date = "2026-03-01",
            stockName = "TCS",
            direction = TradeDirection.LONG,
            entryPrice = 3500.0,
            exitPrice = 3480.0,
            quantity = 10,
            stopLoss = 3480.0,
            target = 3550.0,
            charges = 20.0,
            setup = "Support Bounce",
            mistakes = listOf("FOMO"),
            emotion = "Anxious",
            notes = "Hit stoploss",
            grossPnL = -200.0,
            netPnL = -220.0,
            riskAmount = 200.0,
            rMultiple = -1.1,
            result = TradeResult.LOSS
        )

        val metrics = AnalyticsEngine.calculateDashboardMetrics(listOf(trade1, trade2), 100000.0)

        assertEquals(2, metrics.totalTrades)
        assertEquals(1, metrics.winningTrades)
        assertEquals(1, metrics.losingTrades)
        assertEquals(50.0, metrics.winRate, 0.01)
        assertEquals(260.0, metrics.netProfit, 0.01) // 480 - 220
        assertEquals(100260.0, metrics.currentCapital, 0.01)
    }
}
