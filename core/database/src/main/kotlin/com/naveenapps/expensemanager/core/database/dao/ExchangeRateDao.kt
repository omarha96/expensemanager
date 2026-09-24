package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM exchange_rates WHERE base_code = :baseCode AND target_code = :targetCode")
    suspend fun findRate(baseCode: String, targetCode: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rates WHERE base_code = :baseCode")
    suspend fun findRatesForBase(baseCode: String): List<ExchangeRateEntity>
}
