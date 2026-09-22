package com.naveenapps.expensemanager.core.model

/**
 * A cached conversion rate from [baseCode] to [targetCode], both ISO 4217 currency codes.
 * [fetchedAt] (epoch millis) drives the cache-staleness check before a network refresh.
 */
data class ExchangeRate(
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val fetchedAt: Long,
)
