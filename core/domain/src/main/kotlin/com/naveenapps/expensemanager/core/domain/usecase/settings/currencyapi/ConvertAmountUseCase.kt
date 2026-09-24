package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository

/**
 * Converts [amount] from [fromCode] to [toCode]. When the codes match, or either is blank
 * (an account/transaction created before per-account currencies existed), or no rate is
 * available (no API profile configured, offline with no cache), returns [amount] unchanged
 * rather than failing — a stale or missing rate should degrade to "show the raw number", not
 * break the dashboard/statistics screens.
 */
class ConvertAmountUseCase(
    private val repository: CurrencyApiRepository,
) {
    suspend operator fun invoke(amount: Double, fromCode: String, toCode: String): Double {
        if (fromCode.isBlank() || toCode.isBlank() || fromCode == toCode) return amount
        val rate = repository.getRate(fromCode, toCode) ?: return amount
        return amount * rate
    }
}
