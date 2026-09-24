package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/** Local cache of the last-fetched conversion rate for a base/target currency pair. */
@Entity(tableName = "exchange_rates", primaryKeys = ["base_code", "target_code"])
data class ExchangeRateEntity(
    @ColumnInfo(name = "base_code")
    val baseCode: String,
    @ColumnInfo(name = "target_code")
    val targetCode: String,
    @ColumnInfo(name = "rate")
    val rate: Double,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,
)
