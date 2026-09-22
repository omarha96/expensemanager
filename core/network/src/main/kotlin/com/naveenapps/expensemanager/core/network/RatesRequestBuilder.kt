package com.naveenapps.expensemanager.core.network

import com.naveenapps.expensemanager.core.model.CurrencyApiPreset
import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * Request shape (endpoint path, auth placement) and response shape both vary per provider.
 * Add a new provider by adding a case here rather than changing [ExchangeRateApiService].
 */
internal object RatesRequestBuilder {

    fun buildUrl(profile: CurrencyApiProfile, baseCurrencyCode: String): String {
        val base = profile.baseUrl.trimEnd('/')
        return when (profile.preset) {
            CurrencyApiPreset.FRANKFURTER -> "$base/latest?base=$baseCurrencyCode"
            CurrencyApiPreset.EXCHANGERATE_HOST -> "$base/latest?base=$baseCurrencyCode"
            CurrencyApiPreset.OPEN_EXCHANGE_RATES -> "$base/latest.json?base=$baseCurrencyCode"
            CurrencyApiPreset.CUSTOM -> "$base?base=$baseCurrencyCode"
        }
    }

    fun buildHeaders(profile: CurrencyApiProfile): Map<String, String> {
        if (profile.apiKey.isBlank()) return emptyMap()
        return when (profile.preset) {
            CurrencyApiPreset.OPEN_EXCHANGE_RATES -> emptyMap()
            else -> mapOf("Authorization" to "Bearer ${profile.apiKey}")
        }
    }

    fun buildQuery(profile: CurrencyApiProfile): Map<String, String> {
        if (profile.apiKey.isBlank()) return emptyMap()
        return when (profile.preset) {
            CurrencyApiPreset.OPEN_EXCHANGE_RATES -> mapOf("app_id" to profile.apiKey)
            CurrencyApiPreset.EXCHANGERATE_HOST -> mapOf("access_key" to profile.apiKey)
            else -> emptyMap()
        }
    }

    /** Every supported shape ultimately normalizes to `{ "rates": { "CODE": number } }`. */
    fun parseRates(preset: CurrencyApiPreset, body: JsonObject): Map<String, Double> {
        val ratesNode = body["rates"]?.jsonObject ?: return emptyMap()
        return ratesNode.mapNotNull { (code, value) ->
            val rate = value.jsonPrimitive.doubleOrNull ?: return@mapNotNull null
            code to rate
        }.toMap()
    }
}
