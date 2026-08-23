package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradingRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM trading_rules ORDER BY id ASC")
    fun getAllRulesFlow(): Flow<List<TradingRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: TradingRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rules: List<TradingRuleEntity>)

    @Update
    suspend fun updateRule(rule: TradingRuleEntity)

    @Delete
    suspend fun deleteRule(rule: TradingRuleEntity)

    @Query("DELETE FROM trading_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)
}
