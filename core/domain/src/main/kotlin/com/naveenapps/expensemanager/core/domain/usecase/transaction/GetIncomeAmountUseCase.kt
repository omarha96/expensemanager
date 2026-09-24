package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetIncomeAmountUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
) {

    operator fun invoke(): Flow<Double?> {
        return combine(
            getTransactionWithFilterUseCase.invoke(),
            getCurrencyUseCase.invoke(),
        ) { transactions, currency ->
            var total = 0.0
            transactions?.filter { it.type == TransactionType.INCOME }?.forEach {
                total += convertAmountUseCase(
                    amount = it.amount.amount,
                    fromCode = it.currencyCode.ifBlank { currency.code },
                    toCode = currency.code,
                )
            }
            total
        }
    }
}
