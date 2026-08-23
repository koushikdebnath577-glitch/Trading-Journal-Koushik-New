package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY date DESC, entryTime DESC, id DESC")
    fun getAllTradesFlow(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades ORDER BY date ASC, entryTime ASC, id ASC")
    fun getAllTradesAscFlow(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE id = :id")
    fun getTradeById(id: Long): Flow<TradeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeEntity)

    @Query("DELETE FROM trades WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trades")
    suspend fun clearAllTrades()
}
