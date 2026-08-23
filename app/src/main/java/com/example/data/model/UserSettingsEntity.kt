package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val startingCapital: Double = 100000.0,
    val currencySymbol: String = "₹",
    val defaultRiskAmount: Double = 1000.0,
    val maxRiskPerTrade: Double = 2000.0,
    val maxRiskPercentPerTrade: Double = 2.0,
    val maxDailyLoss: Double = 3000.0,
    val maxTradesPerDay: Int = 3,
    val themeMode: String = "SYSTEM" // "SYSTEM", "DARK", "LIGHT"
)
