package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import kotlinx.coroutines.flow.Flow

interface CurrencyApiRepository {

    fun getProfile(): Flow<CurrencyApiProfile?>

    suspend fun saveProfile(profile: CurrencyApiProfile)

    /**
     * Fetches latest rates from [baseCurrencyCode] using the saved profile. Returns failure when
     * no profile is configured, or the request/parse fails — callers fall back to cached rates.
     */
    suspend fun fetchLatestRates(baseCurrencyCode: String): Result<Map<String, Double>>

    /**
     * Returns the conversion rate from [fromCode] to [toCode], preferring a fresh cached value,
     * refreshing from the upstream API when the cache is missing or stale, and falling back to a
     * stale cached value when the refresh fails (offline resilience). Returns null only when no
     * rate is available at all — no cache and no reachable API.
     */
    suspend fun getRate(fromCode: String, toCode: String): Double?
}
