package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_rules")
data class TradingRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleText: String,
    val category: String = "Risk",
    val isActive: Boolean = true,
    val isSystemRule: Boolean = false
)
