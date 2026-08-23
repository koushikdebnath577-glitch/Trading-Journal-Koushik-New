package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.TradeEntity
import com.example.data.model.TradingRuleEntity
import com.example.data.model.TradingSetupEntity
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.TradingRepository
import com.example.domain.AnalyticsEngine
import com.example.domain.CumulativeTradePoint
import com.example.domain.DailyPerformance
import com.example.domain.DashboardMetrics
import com.example.domain.MistakeAnalysis
import com.example.domain.MonthlyPerformance
import com.example.domain.PerformanceHighlights
import com.example.domain.Trade
import com.example.domain.TradeResult
import com.example.domain.WeeklyPerformance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class JournalPeriodFilter {
    ALL, TODAY, THIS_WEEK, THIS_MONTH
}

enum class JournalResultFilter {
    ALL, WINS, LOSSES
}

private data class FilterState(
    val period: JournalPeriodFilter,
    val result: JournalResultFilter,
    val setup: String,
    val stock: String,
    val query: String
)

class TradingJournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TradingRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = TradingRepository(database)
        viewModelScope.launch {
            repository.ensureDefaultsInitialized()
        }
    }

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            settings ?: UserSettingsEntity()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettingsEntity()
        )

    val allSetups: StateFlow<List<TradingSetupEntity>> = repository.allSetups
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allRules: StateFlow<List<TradingRuleEntity>> = repository.allRules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allTradesDesc: StateFlow<List<Trade>> = repository.allTradesDesc
        .combine(MutableStateFlow(Unit)) { entities, _ ->
            entities.map { Trade.fromEntity(it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allTradesAsc: StateFlow<List<Trade>> = repository.allTradesAsc
        .combine(MutableStateFlow(Unit)) { entities, _ ->
            entities.map { Trade.fromEntity(it) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Dashboard metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allTradesAsc,
        userSettings
    ) { trades, settings ->
        AnalyticsEngine.calculateDashboardMetrics(trades, settings.startingCapital)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DashboardMetrics(
            startingCapital = 100000.0,
            currentCapital = 100000.0,
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
    )

    // Cumulative Points for Equity Curve
    val cumulativePoints: StateFlow<List<CumulativeTradePoint>> = combine(
        allTradesAsc,
        userSettings
    ) { trades, settings ->
        AnalyticsEngine.computeCumulativePoints(trades, settings.startingCapital)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Analytics breakdowns
    val dailyPerformanceList: StateFlow<List<DailyPerformance>> = allTradesAsc
        .combine(MutableStateFlow(Unit)) { trades, _ ->
            AnalyticsEngine.calculateDailyPerformance(trades)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val weeklyPerformanceList: StateFlow<List<WeeklyPerformance>> = allTradesAsc
        .combine(MutableStateFlow(Unit)) { trades, _ ->
            AnalyticsEngine.calculateWeeklyPerformance(trades)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val monthlyPerformanceList: StateFlow<List<MonthlyPerformance>> = combine(
        allTradesAsc,
        userSettings
    ) { trades, settings ->
        AnalyticsEngine.calculateMonthlyPerformance(trades, settings.startingCapital)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val mistakeAnalysis: StateFlow<MistakeAnalysis> = allTradesAsc
        .combine(MutableStateFlow(Unit)) { trades, _ ->
            AnalyticsEngine.calculateMistakeAnalysis(trades)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MistakeAnalysis(
                mostCommonMistake = "None",
                totalTradesWithMistakes = 0,
                totalPnLFromMistakes = 0.0,
                winRateWithMistakes = 0.0,
                winRateWithoutMistakes = 0.0,
                biggestLossMistake = "None",
                mistakeBreakdown = emptyList()
            )
        )

    // Filter and search state for Journal Screen
    val journalPeriodFilter = MutableStateFlow(JournalPeriodFilter.ALL)
    val journalResultFilter = MutableStateFlow(JournalResultFilter.ALL)
    val journalSetupFilter = MutableStateFlow("ALL")
    val journalStockFilter = MutableStateFlow("ALL")
    val journalSearchQuery = MutableStateFlow("")

    private val filtersFlow = combine(
        journalPeriodFilter,
        journalResultFilter,
        journalSetupFilter,
        journalStockFilter,
        journalSearchQuery
    ) { period, result, setup, stock, query ->
        FilterState(period, result, setup, stock, query)
    }

    val filteredTrades: StateFlow<List<Trade>> = combine(
        allTradesDesc,
        filtersFlow
    ) { trades: List<Trade>, filters: FilterState ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentMonthPrefix = if (todayStr.length >= 7) todayStr.substring(0, 7) else ""

        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        trades.filter { trade ->
            // Period filter
            val matchesPeriod = when (filters.period) {
                JournalPeriodFilter.ALL -> true
                JournalPeriodFilter.TODAY -> trade.date == todayStr
                JournalPeriodFilter.THIS_MONTH -> trade.date.startsWith(currentMonthPrefix)
                JournalPeriodFilter.THIS_WEEK -> {
                    try {
                        val d = sdf.parse(trade.date)
                        if (d != null) {
                            cal.time = d
                            cal.get(Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(Calendar.YEAR) == currentYear
                        } else false
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            // Result filter
            val matchesResult = when (filters.result) {
                JournalResultFilter.ALL -> true
                JournalResultFilter.WINS -> trade.result == TradeResult.WIN
                JournalResultFilter.LOSSES -> trade.result == TradeResult.LOSS
            }

            // Setup filter
            val matchesSetup = filters.setup == "ALL" || trade.setup.equals(filters.setup, ignoreCase = true)

            // Stock filter
            val matchesStock = filters.stock == "ALL" || trade.stockName.equals(filters.stock, ignoreCase = true)

            // Search query
            val matchesQuery = if (filters.query.isBlank()) true else {
                val q = filters.query.trim().lowercase(Locale.getDefault())
                trade.stockName.lowercase(Locale.getDefault()).contains(q) ||
                        trade.setup.lowercase(Locale.getDefault()).contains(q) ||
                        trade.notes.lowercase(Locale.getDefault()).contains(q) ||
                        trade.mistakes.any { it.lowercase(Locale.getDefault()).contains(q) } ||
                        trade.emotion.lowercase(Locale.getDefault()).contains(q)
            }

            matchesPeriod && matchesResult && matchesSetup && matchesStock && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Today's trading stats and rule warning checks
    val todayWarningState: StateFlow<TodayWarningState> = combine(
        allTradesAsc,
        userSettings
    ) { trades, settings ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayTrades = trades.filter { it.date == todayStr }
        val count = todayTrades.size
        val netPnL = todayTrades.sumOf { it.netPnL }

        val maxTradesExceeded = count >= settings.maxTradesPerDay
        val maxLossExceeded = settings.maxDailyLoss > 0 && netPnL <= -settings.maxDailyLoss

        TodayWarningState(
            todayTradeCount = count,
            todayNetPnL = netPnL,
            maxTradesPerDay = settings.maxTradesPerDay,
            maxDailyLoss = settings.maxDailyLoss,
            isMaxTradesWarning = maxTradesExceeded,
            isMaxLossWarning = maxLossExceeded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TodayWarningState()
    )

    // Calendar state
    val calendarYearMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))

    // Actions
    fun setCalendarMonth(yearMonth: String) {
        calendarYearMonth.value = yearMonth
    }

    fun addTrade(trade: TradeEntity, onComplete: (() -> Unit)? = null) {
        saveTrade(trade, onComplete)
    }

    fun updateTrade(trade: TradeEntity, onComplete: (() -> Unit)? = null) {
        saveTrade(trade, onComplete)
    }

    fun saveTrade(trade: TradeEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (trade.id == 0L) {
                repository.insertTrade(trade)
            } else {
                repository.updateTrade(trade)
            }
            onComplete?.invoke()
        }
    }

    fun deleteTrade(trade: Trade, onComplete: (() -> Unit)? = null) {
        deleteTrade(trade.id, onComplete)
    }

    fun deleteTrade(tradeId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteTradeById(tradeId)
            onComplete?.invoke()
        }
    }

    fun addSetup(name: String, desc: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertSetup(
                TradingSetupEntity(
                    name = name.trim(),
                    description = desc.trim(),
                    isDefault = false
                )
            )
        }
    }

    fun deleteSetup(setup: TradingSetupEntity) {
        viewModelScope.launch {
            repository.deleteSetup(setup)
        }
    }

    fun addRule(rule: TradingRuleEntity) {
        viewModelScope.launch {
            repository.insertRule(rule)
        }
    }

    fun toggleRule(rule: TradingRuleEntity, isActive: Boolean? = null) {
        viewModelScope.launch {
            val targetActive = isActive ?: !rule.isActive
            repository.updateRule(rule.copy(isActive = targetActive))
        }
    }

    fun deleteRule(rule: TradingRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun clearAllTrades(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllTrades()
            onComplete?.invoke()
        }
    }
}

data class TodayWarningState(
    val todayTradeCount: Int = 0,
    val todayNetPnL: Double = 0.0,
    val maxTradesPerDay: Int = 3,
    val maxDailyLoss: Double = 3000.0,
    val isMaxTradesWarning: Boolean = false,
    val isMaxLossWarning: Boolean = false
)
