package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TradingSetupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetupDao {
    @Query("SELECT * FROM trading_setups ORDER BY name ASC")
    fun getAllSetupsFlow(): Flow<List<TradingSetupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: TradingSetupEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(setups: List<TradingSetupEntity>)

    @Delete
    suspend fun deleteSetup(setup: TradingSetupEntity)

    @Query("DELETE FROM trading_setups WHERE id = :id")
    suspend fun deleteSetupById(id: Long)
}
