package com.naveenapps.expensemanager.core.network

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile

interface ExchangeRateApi {
    /**
     * Fetches the latest rates from [baseCurrencyCode] to every other currency the provider
     * supports, using the user-supplied [profile] (base URL, preset, API key). Returns a map of
     * ISO 4217 code -> rate. Throws on network/parse failure; callers decide the offline fallback.
     */
    suspend fun getLatestRates(
        profile: CurrencyApiProfile,
        baseCurrencyCode: String,
    ): Map<String, Double>
}

internal class ExchangeRateApiImpl(
    private val service: ExchangeRateApiService,
) : ExchangeRateApi {

    override suspend fun getLatestRates(
        profile: CurrencyApiProfile,
        baseCurrencyCode: String,
    ): Map<String, Double> {
        val response = service.getLatestRates(
            url = RatesRequestBuilder.buildUrl(profile, baseCurrencyCode),
            headers = RatesRequestBuilder.buildHeaders(profile),
            query = RatesRequestBuilder.buildQuery(profile),
        )
        val body = response.body()
        check(response.isSuccessful && body != null) {
            "Exchange rate request failed: ${response.code()} ${response.message()}"
        }
        return RatesRequestBuilder.parseRates(profile.preset, body)
    }
}
