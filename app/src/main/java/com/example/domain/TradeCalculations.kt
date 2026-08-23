package com.example.domain

import com.example.data.model.TradeEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class TradeResult {
    WIN, LOSS, BREAKEVEN
}

data class Trade(
    val entity: TradeEntity,
    val grossPnL: Double,
    val netPnL: Double,
    val riskAmount: Double,
    val rMultiple: Double,
    val result: TradeResult
) {
    val id: Long get() = entity.id
    val date: String get() = entity.date
    val entryTime: String get() = entity.entryTime
    val exitTime: String get() = entity.exitTime
    val stockName: String get() = entity.stockName
    val setup: String get() = entity.setup
    val direction: String get() = entity.direction
    val entryPrice: Double get() = entity.entryPrice
    val exitPrice: Double get() = entity.exitPrice
    val stopLoss: Double get() = entity.stopLoss
    val targetPrice: Double get() = entity.targetPrice
    val quantity: Int get() = entity.quantity
    val charges: Double get() = entity.charges
    val manualRiskAmount: Double? get() = entity.manualRiskAmount
    val emotion: String get() = entity.emotion
    val mistakes: List<String> get() = entity.mistakes.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val notes: String get() = entity.notes
    val chartImageUri: String? get() = entity.chartImageUri

    companion object {
        fun fromEntity(entity: TradeEntity): Trade {
            val isBuy = entity.direction.equals("BUY", ignoreCase = true) ||
                    entity.direction.equals("LONG", ignoreCase = true)

            // Gross P&L
            val gross = if (isBuy) {
                (entity.exitPrice - entity.entryPrice) * entity.quantity
            } else {
                (entity.entryPrice - entity.exitPrice) * entity.quantity
            }

            // Net P&L
            val net = gross - entity.charges

            // Risk Amount
            val calculatedRisk = if (entity.manualRiskAmount != null && entity.manualRiskAmount > 0) {
                entity.manualRiskAmount
            } else if (isBuy) {
                if (entity.stopLoss > 0 && entity.entryPrice > entity.stopLoss) {
                    (entity.entryPrice - entity.stopLoss) * entity.quantity
                } else {
                    max(1.0, entity.entryPrice * 0.01 * entity.quantity)
                }
            } else {
                if (entity.stopLoss > 0 && entity.stopLoss > entity.entryPrice) {
                    (entity.stopLoss - entity.entryPrice) * entity.quantity
                } else {
                    max(1.0, entity.entryPrice * 0.01 * entity.quantity)
                }
            }

            val risk = max(1.0, calculatedRisk)

            // R Multiple
            val rMultiple = net / risk

            // Result
            val result = when {
                net > 0.01 -> TradeResult.WIN
                net < -0.01 -> TradeResult.LOSS
                else -> TradeResult.BREAKEVEN
            }

            return Trade(
                entity = entity,
                grossPnL = gross,
                netPnL = net,
                riskAmount = risk,
                rMultiple = rMultiple,
                result = result
            )
        }
    }
}

data class CumulativeTradePoint(
    val tradeIndex: Int,
    val date: String,
    val stockName: String,
    val tradeNetPnL: Double,
    val cumulativeNetPnL: Double,
    val currentEquity: Double,
    val peakEquity: Double,
    val drawdownAmount: Double,
    val drawdownPercent: Double
)

data class DashboardMetrics(
    val startingCapital: Double,
    val currentCapital: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val breakevenTrades: Int,
    val winRate: Double,
    val lossRate: Double,
    val averageR: Double,
    val averageWinR: Double,
    val averageLossR: Double,
    val expectancy: Double,
    val profitFactor: Double,
    val totalGrossProfit: Double,
    val totalGrossLoss: Double,
    val totalCharges: Double,
    val netProfit: Double,
    val netProfitPercent: Double,
    val totalReturnPercent: Double,
    val currentDrawdown: Double,
    val currentDrawdownPercent: Double,
    val maxDrawdown: Double,
    val maxDrawdownPercent: Double,
    val setupPerformanceList: List<SetupPerformance>,
    val highlights: PerformanceHighlights
)

data class SetupPerformance(
    val setupName: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double,
    val averageR: Double,
    val averageWinR: Double,
    val totalNetPnL: Double,
    val profitFactor: Double,
    val expectancy: Double
)

data class DailyPerformance(
    val date: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val grossPnL: Double,
    val totalCharges: Double,
    val netPnL: Double,
    val totalR: Double,
    val winRate: Double,
    val trades: List<Trade>
)

data class WeeklyPerformance(
    val weekLabel: String,
    val startDate: String,
    val endDate: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double,
    val netPnL: Double,
    val averageR: Double,
    val expectancy: Double,
    val profitFactor: Double
)

data class MonthlyPerformance(
    val yearMonth: String, // e.g. "2026-08"
    val displayMonth: String, // e.g. "August 2026"
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double,
    val netPnL: Double,
    val netProfitPercent: Double,
    val averageR: Double,
    val averageWinR: Double,
    val expectancy: Double,
    val profitFactor: Double,
    val bestSetup: String,
    val worstSetup: String,
    val maxDrawdown: Double
)

data class MistakeStat(
    val mistakeName: String,
    val count: Int,
    val totalLossPnL: Double,
    val netPnL: Double
)

data class MistakeAnalysis(
    val mostCommonMistake: String,
    val totalTradesWithMistakes: Int,
    val totalPnLFromMistakes: Double,
    val winRateWithMistakes: Double,
    val winRateWithoutMistakes: Double,
    val biggestLossMistake: String,
    val mistakeBreakdown: List<MistakeStat>
)

data class PerformanceHighlights(
    val bestTrade: Trade?,
    val worstTrade: Trade?,
    val highestRTrade: Trade?,
    val largestLossTrade: Trade?,
    val bestDay: DailyPerformance?,
    val worstDay: DailyPerformance?,
    val bestSetup: String,
    val worstSetup: String
)

object AnalyticsEngine {

    fun computeCumulativePoints(
        tradesAsc: List<Trade>,
        startingCapital: Double
    ): List<CumulativeTradePoint> {
        val points = mutableListOf<CumulativeTradePoint>()
        var runningCumulativePnL = 0.0
        var peak = startingCapital

        tradesAsc.forEachIndexed { index, trade ->
            runningCumulativePnL += trade.netPnL
            val equity = startingCapital + runningCumulativePnL
            if (equity > peak) {
                peak = equity
            }
            val ddAmount = max(0.0, peak - equity)
            val ddPercent = if (peak > 0) (ddAmount / peak) * 100 else 0.0

            points.add(
                CumulativeTradePoint(
                    tradeIndex = index + 1,
                    date = trade.date,
                    stockName = trade.stockName,
                    tradeNetPnL = trade.netPnL,
                    cumulativeNetPnL = runningCumulativePnL,
                    currentEquity = equity,
                    peakEquity = peak,
                    drawdownAmount = ddAmount,
                    drawdownPercent = ddPercent
                )
            )
        }
        return points
    }

    fun calculateDashboardMetrics(
        allTradesAsc: List<Trade>,
        startingCapital: Double
    ): DashboardMetrics {
        val totalTrades = allTradesAsc.size
        if (totalTrades == 0) {
            return DashboardMetrics(
                startingCapital = startingCapital,
                currentCapital = startingCapital,
                totalTrades = 0,
                winningTrades = 0,
                losingTrades = 0,
                breakevenTrades = 0,
                winRate = 0.0,
                lossRate = 0.0,
                averageR = 0.0,
                averageWinR = 0.0,
                averageLossR = 0.0,
                expectancy = 0.0,
                profitFactor = 0.0,
                totalGrossProfit = 0.0,
                totalGrossLoss = 0.0,
                totalCharges = 0.0,
                netProfit = 0.0,
                netProfitPercent = 0.0,
                totalReturnPercent = 0.0,
                currentDrawdown = 0.0,
                currentDrawdownPercent = 0.0,
                maxDrawdown = 0.0,
                maxDrawdownPercent = 0.0,
                setupPerformanceList = emptyList(),
                highlights = PerformanceHighlights(
                    bestTrade = null,
                    worstTrade = null,
                    highestRTrade = null,
                    largestLossTrade = null,
                    bestDay = null,
                    worstDay = null,
                    bestSetup = "None",
                    worstSetup = "None"
                )
            )
        }

        val winningTrades = allTradesAsc.filter { it.result == TradeResult.WIN }
        val losingTrades = allTradesAsc.filter { it.result == TradeResult.LOSS }
        val breakevenTrades = allTradesAsc.filter { it.result == TradeResult.BREAKEVEN }

        val winRate = (winningTrades.size.toDouble() / totalTrades) * 100
        val lossRate = (losingTrades.size.toDouble() / totalTrades) * 100

        val totalR = allTradesAsc.sumOf { it.rMultiple }
        val avgR = totalR / totalTrades
        val avgWinR = if (winningTrades.isNotEmpty()) winningTrades.sumOf { it.rMultiple } / winningTrades.size else 0.0
        val avgLossR = if (losingTrades.isNotEmpty()) losingTrades.sumOf { it.rMultiple } / losingTrades.size else 0.0
        val expectancy = avgR

        var totalGrossProfit = 0.0
        var totalGrossLoss = 0.0
        var totalCharges = 0.0
        allTradesAsc.forEach { t ->
            if (t.grossPnL > 0) totalGrossProfit += t.grossPnL
            else totalGrossLoss += t.grossPnL
            totalCharges += t.charges
        }

        val netProfit = totalGrossProfit + totalGrossLoss - totalCharges
        val currentCapital = startingCapital + netProfit
        val netProfitPercent = if (startingCapital > 0) (netProfit / startingCapital) * 100 else 0.0
        val totalReturnPercent = netProfitPercent

        val profitFactor = if (abs(totalGrossLoss) > 0.001) {
            totalGrossProfit / abs(totalGrossLoss)
        } else if (totalGrossProfit > 0) {
            100.0
        } else 0.0

        val cumulativePoints = computeCumulativePoints(allTradesAsc, startingCapital)
        val maxDdAmount = cumulativePoints.maxOfOrNull { it.drawdownAmount } ?: 0.0
        val maxDdPercent = cumulativePoints.maxOfOrNull { it.drawdownPercent } ?: 0.0
        val currentDd = cumulativePoints.lastOrNull()?.drawdownAmount ?: 0.0
        val currentDdPercent = cumulativePoints.lastOrNull()?.drawdownPercent ?: 0.0

        val setupList = calculateSetupPerformance(allTradesAsc)
        val highlights = calculateHighlights(allTradesAsc, startingCapital)

        return DashboardMetrics(
            startingCapital = startingCapital,
            currentCapital = currentCapital,
            totalTrades = totalTrades,
            winningTrades = winningTrades.size,
            losingTrades = losingTrades.size,
            breakevenTrades = breakevenTrades.size,
            winRate = winRate,
            lossRate = lossRate,
            averageR = avgR,
            averageWinR = avgWinR,
            averageLossR = avgLossR,
            expectancy = expectancy,
            profitFactor = profitFactor,
            totalGrossProfit = totalGrossProfit,
            totalGrossLoss = totalGrossLoss,
            totalCharges = totalCharges,
            netProfit = netProfit,
            netProfitPercent = netProfitPercent,
            totalReturnPercent = totalReturnPercent,
            currentDrawdown = currentDd,
            currentDrawdownPercent = currentDdPercent,
            maxDrawdown = maxDdAmount,
            maxDrawdownPercent = maxDdPercent,
            setupPerformanceList = setupList,
            highlights = highlights
        )
    }

    fun calculateSetupPerformance(trades: List<Trade>): List<SetupPerformance> {
        val grouped = trades.groupBy { it.setup.ifBlank { "Unassigned" } }
        return grouped.map { (setupName, setupTrades) ->
            val total = setupTrades.size
            val wins = setupTrades.count { it.result == TradeResult.WIN }
            val losses = setupTrades.count { it.result == TradeResult.LOSS }
            val winRate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0
            val totalR = setupTrades.sumOf { it.rMultiple }
            val avgR = if (total > 0) totalR / total else 0.0
            val winTrades = setupTrades.filter { it.result == TradeResult.WIN }
            val avgWinR = if (winTrades.isNotEmpty()) winTrades.sumOf { it.rMultiple } / winTrades.size else 0.0
            val netPnL = setupTrades.sumOf { it.netPnL }

            val grossProfit = setupTrades.filter { it.grossPnL > 0 }.sumOf { it.grossPnL }
            val grossLoss = abs(setupTrades.filter { it.grossPnL < 0 }.sumOf { it.grossPnL })
            val pf = if (grossLoss > 0.001) grossProfit / grossLoss else if (grossProfit > 0) 100.0 else 0.0

            SetupPerformance(
                setupName = setupName,
                totalTrades = total,
                winningTrades = wins,
                losingTrades = losses,
                winRate = winRate,
                averageR = avgR,
                averageWinR = avgWinR,
                totalNetPnL = netPnL,
                profitFactor = pf,
                expectancy = avgR
            )
        }.sortedByDescending { it.totalNetPnL }
    }

    fun calculateDailyPerformance(trades: List<Trade>): List<DailyPerformance> {
        val grouped = trades.groupBy { it.date }
        return grouped.map { (dateStr, dayTrades) ->
            val total = dayTrades.size
            val wins = dayTrades.count { it.result == TradeResult.WIN }
            val losses = dayTrades.count { it.result == TradeResult.LOSS }
            val gross = dayTrades.sumOf { it.grossPnL }
            val charges = dayTrades.sumOf { it.charges }
            val net = dayTrades.sumOf { it.netPnL }
            val totalR = dayTrades.sumOf { it.rMultiple }
            val winRate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0

            DailyPerformance(
                date = dateStr,
                totalTrades = total,
                winningTrades = wins,
                losingTrades = losses,
                grossPnL = gross,
                totalCharges = charges,
                netPnL = net,
                totalR = totalR,
                winRate = winRate,
                trades = dayTrades.sortedByDescending { it.entryTime }
            )
        }.sortedByDescending { it.date }
    }

    fun calculateWeeklyPerformance(trades: List<Trade>): List<WeeklyPerformance> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val grouped = trades.groupBy { trade ->
            try {
                val parsed = sdf.parse(trade.date) ?: Date()
                cal.time = parsed
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                val year = cal.get(Calendar.YEAR)
                "$year-W${String.format("%02d", week)}"
            } catch (e: Exception) {
                "Unknown Week"
            }
        }

        return grouped.map { (weekKey, weekTrades) ->
            val total = weekTrades.size
            val wins = weekTrades.count { it.result == TradeResult.WIN }
            val losses = weekTrades.count { it.result == TradeResult.LOSS }
            val winRate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0
            val netPnL = weekTrades.sumOf { it.netPnL }
            val avgR = if (total > 0) weekTrades.sumOf { it.rMultiple } / total else 0.0

            val grossProfit = weekTrades.filter { it.grossPnL > 0 }.sumOf { it.grossPnL }
            val grossLoss = abs(weekTrades.filter { it.grossPnL < 0 }.sumOf { it.grossPnL })
            val pf = if (grossLoss > 0.001) grossProfit / grossLoss else if (grossProfit > 0) 100.0 else 0.0

            val sortedDates = weekTrades.map { it.date }.sorted()
            val start = sortedDates.firstOrNull() ?: ""
            val end = sortedDates.lastOrNull() ?: ""

            WeeklyPerformance(
                weekLabel = weekKey,
                startDate = start,
                endDate = end,
                totalTrades = total,
                winningTrades = wins,
                losingTrades = losses,
                winRate = winRate,
                netPnL = netPnL,
                averageR = avgR,
                expectancy = avgR,
                profitFactor = pf
            )
        }.sortedByDescending { it.weekLabel }
    }

    fun calculateMonthlyPerformance(trades: List<Trade>, startingCapital: Double): List<MonthlyPerformance> {
        val grouped = trades.groupBy { trade ->
            if (trade.date.length >= 7) trade.date.substring(0, 7) else "Unknown"
        }

        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        return grouped.map { (ym, mTrades) ->
            val total = mTrades.size
            val wins = mTrades.count { it.result == TradeResult.WIN }
            val losses = mTrades.count { it.result == TradeResult.LOSS }
            val winRate = if (total > 0) (wins.toDouble() / total) * 100 else 0.0
            val netPnL = mTrades.sumOf { it.netPnL }
            val netProfitPct = if (startingCapital > 0) (netPnL / startingCapital) * 100 else 0.0
            val avgR = if (total > 0) mTrades.sumOf { it.rMultiple } / total else 0.0

            val winTrades = mTrades.filter { it.result == TradeResult.WIN }
            val avgWinR = if (winTrades.isNotEmpty()) winTrades.sumOf { it.rMultiple } / winTrades.size else 0.0

            val grossProfit = mTrades.filter { it.grossPnL > 0 }.sumOf { it.grossPnL }
            val grossLoss = abs(mTrades.filter { it.grossPnL < 0 }.sumOf { it.grossPnL })
            val pf = if (grossLoss > 0.001) grossProfit / grossLoss else if (grossProfit > 0) 100.0 else 0.0

            // Setups in this month
            val setupGroups = mTrades.groupBy { it.setup }
            val bestSetup = setupGroups.maxByOrNull { it.value.sumOf { t -> t.netPnL } }?.key ?: "N/A"
            val worstSetup = setupGroups.minByOrNull { it.value.sumOf { t -> t.netPnL } }?.key ?: "N/A"

            // Max Drawdown in this month
            val mPoints = computeCumulativePoints(mTrades.sortedBy { it.date }, startingCapital)
            val maxDd = mPoints.maxOfOrNull { it.drawdownAmount } ?: 0.0

            val displayMonthStr = try {
                val parsed = monthFormat.parse(ym)
                if (parsed != null) displayFormat.format(parsed) else ym
            } catch (e: Exception) {
                ym
            }

            MonthlyPerformance(
                yearMonth = ym,
                displayMonth = displayMonthStr,
                totalTrades = total,
                winningTrades = wins,
                losingTrades = losses,
                winRate = winRate,
                netPnL = netPnL,
                netProfitPercent = netProfitPct,
                averageR = avgR,
                averageWinR = avgWinR,
                expectancy = avgR,
                profitFactor = pf,
                bestSetup = bestSetup,
                worstSetup = worstSetup,
                maxDrawdown = maxDd
            )
        }.sortedByDescending { it.yearMonth }
    }

    fun calculateMistakeAnalysis(trades: List<Trade>): MistakeAnalysis {
        val tradesWithMistakes = trades.filter { trade ->
            val mList = trade.mistakes
            mList.any { !it.equals("No Mistake", ignoreCase = true) && it.isNotBlank() }
        }
        val tradesWithoutMistakes = trades.filter { trade ->
            val mList = trade.mistakes
            mList.isEmpty() || mList.all { it.equals("No Mistake", ignoreCase = true) || it.isBlank() }
        }

        val totalPnLWithMistakes = tradesWithMistakes.sumOf { it.netPnL }
        val winRateWithMistakes = if (tradesWithMistakes.isNotEmpty()) {
            (tradesWithMistakes.count { it.result == TradeResult.WIN }.toDouble() / tradesWithMistakes.size) * 100
        } else 0.0

        val winRateWithoutMistakes = if (tradesWithoutMistakes.isNotEmpty()) {
            (tradesWithoutMistakes.count { it.result == TradeResult.WIN }.toDouble() / tradesWithoutMistakes.size) * 100
        } else 0.0

        val mistakeCountMap = mutableMapOf<String, Int>()
        val mistakeLossMap = mutableMapOf<String, Double>()
        val mistakeNetMap = mutableMapOf<String, Double>()

        trades.forEach { t ->
            t.mistakes.forEach { rawMistake ->
                val m = rawMistake.trim()
                if (m.isNotBlank() && !m.equals("No Mistake", ignoreCase = true)) {
                    mistakeCountMap[m] = (mistakeCountMap[m] ?: 0) + 1
                    mistakeNetMap[m] = (mistakeNetMap[m] ?: 0.0) + t.netPnL
                    if (t.netPnL < 0) {
                        mistakeLossMap[m] = (mistakeLossMap[m] ?: 0.0) + abs(t.netPnL)
                    }
                }
            }
        }

        val breakdown = mistakeCountMap.map { (mistake, count) ->
            MistakeStat(
                mistakeName = mistake,
                count = count,
                totalLossPnL = mistakeLossMap[mistake] ?: 0.0,
                netPnL = mistakeNetMap[mistake] ?: 0.0
            )
        }.sortedByDescending { it.count }

        val mostCommon = breakdown.maxByOrNull { it.count }?.mistakeName ?: "None"
        val biggestLoss = breakdown.maxByOrNull { it.totalLossPnL }?.mistakeName ?: "None"

        return MistakeAnalysis(
            mostCommonMistake = mostCommon,
            totalTradesWithMistakes = tradesWithMistakes.size,
            totalPnLFromMistakes = totalPnLWithMistakes,
            winRateWithMistakes = winRateWithMistakes,
            winRateWithoutMistakes = winRateWithoutMistakes,
            biggestLossMistake = biggestLoss,
            mistakeBreakdown = breakdown
        )
    }

    private fun calculateHighlights(trades: List<Trade>, startingCapital: Double): PerformanceHighlights {
        if (trades.isEmpty()) {
            return PerformanceHighlights(
                bestTrade = null,
                worstTrade = null,
                highestRTrade = null,
                largestLossTrade = null,
                bestDay = null,
                worstDay = null,
                bestSetup = "None",
                worstSetup = "None"
            )
        }

        val bestTrade = trades.maxByOrNull { it.netPnL }
        val worstTrade = trades.minByOrNull { it.netPnL }
        val highestR = trades.maxByOrNull { it.rMultiple }
        val largestLossTrade = trades.minByOrNull { it.netPnL }

        val dailyList = calculateDailyPerformance(trades)
        val bestDay = dailyList.maxByOrNull { it.netPnL }
        val worstDay = dailyList.minByOrNull { it.netPnL }

        val setups = calculateSetupPerformance(trades)
        val bestSetup = setups.maxByOrNull { it.totalNetPnL }?.setupName ?: "None"
        val worstSetup = setups.minByOrNull { it.totalNetPnL }?.setupName ?: "None"

        return PerformanceHighlights(
            bestTrade = bestTrade,
            worstTrade = worstTrade,
            highestRTrade = highestR,
            largestLossTrade = largestLossTrade,
            bestDay = bestDay,
            worstDay = worstDay,
            bestSetup = bestSetup,
            worstSetup = worstSetup
        )
    }
}
