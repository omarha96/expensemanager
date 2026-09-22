package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.CurrencyApiDataStore
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.network.ExchangeRateApi
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CurrencyApiRepositoryImpl(
    private val dataStore: CurrencyApiDataStore,
    private val exchangeRateApi: ExchangeRateApi,
    private val dispatchers: AppCoroutineDispatchers,
) : CurrencyApiRepository {

    override fun getProfile(): Flow<CurrencyApiProfile?> = dataStore.getProfile()

    override suspend fun saveProfile(profile: CurrencyApiProfile) = withContext(dispatchers.io) {
        dataStore.saveProfile(profile)
    }

    override suspend fun fetchLatestRates(
        baseCurrencyCode: String,
    ): Result<Map<String, Double>> = withContext(dispatchers.io) {
        runCatching {
            val profile = dataStore.getProfile().first()
                ?: error("No currency API profile configured")
            exchangeRateApi.getLatestRates(profile, baseCurrencyCode)
        }
    }
}
