package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.dao.ExchangeRateDao
import com.naveenapps.expensemanager.core.database.entity.ExchangeRateEntity
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
    private val exchangeRateDao: ExchangeRateDao,
    private val dispatchers: AppCoroutineDispatchers,
) : CurrencyApiRepository {

    override fun getProfile(): Flow<CurrencyApiProfile?> = dataStore.getProfile()

    override suspend fun saveProfile(profile: CurrencyApiProfile): Unit =
        withContext(dispatchers.io) {
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

    override suspend fun getRate(fromCode: String, toCode: String): Double? =
        withContext(dispatchers.io) {
            val cached = exchangeRateDao.findRate(fromCode, toCode)
            if (cached != null && isFresh(cached.fetchedAt)) {
                return@withContext cached.rate
            }

            val refreshed = fetchLatestRates(fromCode).getOrNull()
            if (refreshed != null) {
                val now = System.currentTimeMillis()
                exchangeRateDao.insertAll(
                    refreshed.map { (targetCode, rate) ->
                        ExchangeRateEntity(
                            baseCode = fromCode,
                            targetCode = targetCode,
                            rate = rate,
                            fetchedAt = now,
                        )
                    },
                )
                return@withContext refreshed[toCode]
            }

            // Refresh failed (offline, no profile, upstream error) — fall back to whatever is
            // cached, even if stale, rather than leaving the caller with nothing.
            cached?.rate
        }

    private fun isFresh(fetchedAt: Long): Boolean {
        return System.currentTimeMillis() - fetchedAt < CACHE_TTL_MILLIS
    }

    companion object {
        private const val CACHE_TTL_MILLIS = 60 * 60 * 1000L // 1 hour
    }
}
