package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.TradeEntity
import com.example.data.model.TradingRuleEntity
import com.example.data.model.TradingSetupEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TradingRepository(private val database: AppDatabase) {

    val allTradesDesc: Flow<List<TradeEntity>> = database.tradeDao().getAllTradesFlow()
    val allTradesAsc: Flow<List<TradeEntity>> = database.tradeDao().getAllTradesAscFlow()
    val allSetups: Flow<List<TradingSetupEntity>> = database.setupDao().getAllSetupsFlow()
    val allRules: Flow<List<TradingRuleEntity>> = database.ruleDao().getAllRulesFlow()
    val userSettings: Flow<UserSettingsEntity?> = database.settingsDao().getSettingsFlow()

    suspend fun insertTrade(trade: TradeEntity): Long = withContext(Dispatchers.IO) {
        database.tradeDao().insertTrade(trade)
    }

    suspend fun updateTrade(trade: TradeEntity) = withContext(Dispatchers.IO) {
        database.tradeDao().updateTrade(trade)
    }

    suspend fun deleteTrade(trade: TradeEntity) = withContext(Dispatchers.IO) {
        database.tradeDao().deleteTrade(trade)
    }

    suspend fun deleteTradeById(id: Long) = withContext(Dispatchers.IO) {
        database.tradeDao().deleteTradeById(id)
    }

    suspend fun clearAllTrades() = withContext(Dispatchers.IO) {
        database.tradeDao().clearAllTrades()
    }

    suspend fun insertSetup(setup: TradingSetupEntity): Long = withContext(Dispatchers.IO) {
        database.setupDao().insertSetup(setup)
    }

    suspend fun deleteSetup(setup: TradingSetupEntity) = withContext(Dispatchers.IO) {
        database.setupDao().deleteSetup(setup)
    }

    suspend fun insertRule(rule: TradingRuleEntity): Long = withContext(Dispatchers.IO) {
        database.ruleDao().insertRule(rule)
    }

    suspend fun updateRule(rule: TradingRuleEntity) = withContext(Dispatchers.IO) {
        database.ruleDao().updateRule(rule)
    }

    suspend fun deleteRule(rule: TradingRuleEntity) = withContext(Dispatchers.IO) {
        database.ruleDao().deleteRule(rule)
    }

    suspend fun updateSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        database.settingsDao().insertOrUpdate(settings)
    }

    suspend fun ensureDefaultsInitialized() = withContext(Dispatchers.IO) {
        val currentSettings = database.settingsDao().getSettingsDirect()
        if (currentSettings == null) {
            database.settingsDao().insertOrUpdate(
                UserSettingsEntity(
                    id = 1,
                    startingCapital = 100000.0,
                    currencySymbol = "₹",
                    defaultRiskAmount = 1000.0,
                    maxRiskPerTrade = 2000.0,
                    maxDailyLoss = 3000.0,
                    maxTradesPerDay = 3,
                    themeMode = "SYSTEM"
                )
            )
            // Ensure default setups
            database.setupDao().insertAll(
                listOf(
                    TradingSetupEntity(name = "Morning Breakout", description = "Opening range breakout", isDefault = true),
                    TradingSetupEntity(name = "Resistance Rejection", description = "Rejection from resistance", isDefault = true),
                    TradingSetupEntity(name = "Support Bounce", description = "Long on key support", isDefault = true),
                    TradingSetupEntity(name = "Trendline Breakout", description = "Breakout of trendline", isDefault = true),
                    TradingSetupEntity(name = "VWAP Pullback", description = "Entry at VWAP retest", isDefault = true)
                )
            )
            // Ensure default rules
            database.ruleDao().insertAll(
                listOf(
                    TradingRuleEntity(ruleText = "Maximum 3 trades per day", isActive = true, isSystemRule = true),
                    TradingRuleEntity(ruleText = "Do not trade without A+ setup", isActive = true, isSystemRule = true),
                    TradingRuleEntity(ruleText = "Strictly follow Stop Loss", isActive = true, isSystemRule = true),
                    TradingRuleEntity(ruleText = "Avoid overtrading and revenge trading", isActive = true, isSystemRule = true),
                    TradingRuleEntity(ruleText = "Minimum 1:2 Risk to Reward target", isActive = true, isSystemRule = false),
                    TradingRuleEntity(ruleText = "Stop trading immediately if max daily loss is reached", isActive = true, isSystemRule = true)
                )
            )
        }
    }
}
