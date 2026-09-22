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
}
