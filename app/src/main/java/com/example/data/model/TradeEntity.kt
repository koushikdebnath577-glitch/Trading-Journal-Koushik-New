package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val entryTime: String = "09:15", // HH:mm
    val exitTime: String = "15:15", // HH:mm
    val stockName: String,
    val setup: String,
    val direction: String, // "BUY" / "SELL"
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLoss: Double,
    val targetPrice: Double = 0.0,
    val quantity: Int,
    val charges: Double = 40.0,
    val manualRiskAmount: Double? = null,
    val emotion: String = "Calm", // Calm, Fear, Greed, FOMO, Angry, Overconfident, Revenge Trading, Neutral
    val mistakes: String = "No Mistake", // Comma-separated mistakes
    val notes: String = "",
    val chartImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
