package com.example.ui.components

import android.content.Context
import android.content.Intent
import com.example.domain.MonthlyPerformance
import com.example.domain.Trade

object CsvExportHelper {

    fun exportTradesToCsv(
        context: Context,
        trades: List<Trade>,
        currencySymbol: String = "₹",
        title: String = "Trades_Export"
    ) {
        val sb = StringBuilder()
        // Header
        sb.append("Trade ID,Date,Entry Time,Exit Time,Stock Name,Direction,Setup,Entry Price,Exit Price,Stop Loss,Target Price,Quantity,Charges,Risk Amount,Gross PnL,Net PnL,R Multiple,Result,Emotion,Mistakes,Notes\n")

        trades.forEach { t ->
            val escapedNotes = escapeCsv(t.notes)
            val escapedMistakes = escapeCsv(t.mistakes.joinToString("; "))
            val escapedStock = escapeCsv(t.stockName)
            val escapedSetup = escapeCsv(t.setup)

            sb.append("${t.id},")
            sb.append("${t.date},")
            sb.append("${t.entryTime},")
            sb.append("${t.exitTime},")
            sb.append("$escapedStock,")
            sb.append("${t.direction},")
            sb.append("$escapedSetup,")
            sb.append(String.format("%.2f,", t.entryPrice))
            sb.append(String.format("%.2f,", t.exitPrice))
            sb.append(String.format("%.2f,", t.stopLoss))
            sb.append(String.format("%.2f,", t.targetPrice))
            sb.append("${t.quantity},")
            sb.append(String.format("%.2f,", t.charges))
            sb.append(String.format("%.2f,", t.riskAmount))
            sb.append(String.format("%.2f,", t.grossPnL))
            sb.append(String.format("%.2f,", t.netPnL))
            sb.append(String.format("%.2f,", t.rMultiple))
            sb.append("${t.result.name},")
            sb.append("${t.emotion},")
            sb.append("$escapedMistakes,")
            sb.append("$escapedNotes\n")
        }

        shareCsvContent(context, sb.toString(), "$title.csv")
    }

    fun exportMonthlySummaryToCsv(
        context: Context,
        monthlyList: List<MonthlyPerformance>,
        currencySymbol: String = "₹"
    ) {
        val sb = StringBuilder()
        sb.append("Month,Display Month,Total Trades,Winning Trades,Losing Trades,Win Rate %,Net PnL ($currencySymbol),Net Profit %,Average R,Avg Win R,Expectancy,Profit Factor,Best Setup,Worst Setup,Max Drawdown\n")

        monthlyList.forEach { m ->
            sb.append("${m.yearMonth},")
            sb.append("${escapeCsv(m.displayMonth)},")
            sb.append("${m.totalTrades},")
            sb.append("${m.winningTrades},")
            sb.append("${m.losingTrades},")
            sb.append(String.format("%.2f,", m.winRate))
            sb.append(String.format("%.2f,", m.netPnL))
            sb.append(String.format("%.2f,", m.netProfitPercent))
            sb.append(String.format("%.2f,", m.averageR))
            sb.append(String.format("%.2f,", m.averageWinR))
            sb.append(String.format("%.2f,", m.expectancy))
            sb.append(String.format("%.2f,", m.profitFactor))
            sb.append("${escapeCsv(m.bestSetup)},")
            sb.append("${escapeCsv(m.worstSetup)},")
            sb.append(String.format("%.2f\n", m.maxDrawdown))
        }

        shareCsvContent(context, sb.toString(), "Monthly_Performance_Export.csv")
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun shareCsvContent(context: Context, csvText: String, filename: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, filename)
            putExtra(Intent.EXTRA_TEXT, csvText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Export Trading Journal CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
