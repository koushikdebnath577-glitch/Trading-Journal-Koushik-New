package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.RuleDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.SetupDao
import com.example.data.dao.TradeDao
import com.example.data.model.TradeEntity
import com.example.data.model.TradingRuleEntity
import com.example.data.model.TradingSetupEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TradeEntity::class,
        TradingSetupEntity::class,
        TradingRuleEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tradeDao(): TradeDao
    abstract fun setupDao(): SetupDao
    abstract fun ruleDao(): RuleDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "koushik_trading_journal.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            prepopulateDatabase(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(db: AppDatabase) {
            // Prepopulate Setups
            val defaultSetups = listOf(
                TradingSetupEntity(name = "Morning Breakout", description = "High volume opening range breakout", isDefault = true),
                TradingSetupEntity(name = "Resistance Rejection", description = "Short/Sell on rejection from key resistance level", isDefault = true),
                TradingSetupEntity(name = "Support Bounce", description = "Long/Buy on successful retest of key support", isDefault = true),
                TradingSetupEntity(name = "Trendline Breakout", description = "Breakout of multi-touch trendline", isDefault = true),
                TradingSetupEntity(name = "VWAP Pullback", description = "Entry at VWAP retest in the direction of the trend", isDefault = true)
            )
            db.setupDao().insertAll(defaultSetups)

            // Prepopulate Rules
            val defaultRules = listOf(
                TradingRuleEntity(ruleText = "Maximum 3 trades per day", isActive = true, isSystemRule = true),
                TradingRuleEntity(ruleText = "Do not trade without A+ setup", isActive = true, isSystemRule = true),
                TradingRuleEntity(ruleText = "Strictly follow Stop Loss (No moving SL further)", isActive = true, isSystemRule = true),
                TradingRuleEntity(ruleText = "Avoid overtrading and revenge trading", isActive = true, isSystemRule = true),
                TradingRuleEntity(ruleText = "Minimum 1:2 Risk to Reward target", isActive = true, isSystemRule = false),
                TradingRuleEntity(ruleText = "Stop trading immediately if max daily loss is reached", isActive = true, isSystemRule = true)
            )
            db.ruleDao().insertAll(defaultRules)

            // Prepopulate Settings
            db.settingsDao().insertOrUpdate(
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
        }
    }
}
