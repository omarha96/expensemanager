package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.sum
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.core.model.isTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

class GetTransactionGroupByCategoryUseCase(
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) {
    fun invoke(categoryType: CategoryType): Flow<CategoryTransactionState> {
        return combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, transactions ->

            // Single pass: filter transfers, group by category, filter by type
            val byCategory = transactions
                ?.filterNot { it.type.isTransfer() }
                ?.groupBy { it.category }
                ?.filterKeys { it.type == categoryType }
                ?: emptyMap()

            // Compute per-category sums once; derive total from those — no double iteration.
            // Each transaction is converted from its own currency to the display currency
            // before summing, so a mix of currencies never gets added together as if they
            // were the same unit.
            val categoryAmounts = byCategory.mapValues { (_, txns) ->
                convertAmountUseCase.total(
                    txns.map { it.amount.amount to it.fromCurrencyCode },
                    currency.code,
                )
            }
            // Percentages and the overall total need every category converted; with a rate
            // missing (or conversion disabled) they're unavailable rather than a partial sum.
            val totalAmount = categoryAmounts.values.sum()
            val allAvailable = totalAmount.value != null
            val totalValue = totalAmount.value ?: 0.0

            val categoryTransactions = byCategory.entries
                .map { (category, txns) ->
                    val spent = categoryAmounts.getValue(category)
                    val spentValue = spent.value
                    CategoryTransaction(
                        category = category,
                        percent = if (allAvailable && spentValue != null && totalValue > 0.0) {
                            (spentValue / totalValue).toFloat() * 100
                        } else {
                            0f
                        },
                        amount = getFormattedAmountUseCase(spent, currency),
                        transaction = txns,
                    )
                }
                .sortedByDescending { it.percent }

            val pieChartData: List<PieChartData>
            val newCategoryTransaction: List<CategoryTransaction>

            if (categoryTransactions.isEmpty()) {
                val categories = getAllCategoryUseCase.invoke().firstOrNull()
                    ?.filter { it.type == categoryType }
                    .orEmpty()
                val equalShare = if (categories.isNotEmpty()) 100f / categories.size else 0f
                newCategoryTransaction = categories.map { category ->
                    CategoryTransaction(
                        category = category,
                        percent = 0f,
                        amount = getFormattedAmountUseCase.invoke(amount = 0.0, currency = currency),
                        transaction = emptyList(),
                    )
                }
                pieChartData = categories.map { getDummyPieChartData(it.name, equalShare, it.titleResId) }
            } else {
                newCategoryTransaction = categoryTransactions
                pieChartData = categoryTransactions.map { it.toChartModel() }
            }

            CategoryTransactionState(
                pieChartData = pieChartData,
                totalAmount = getFormattedAmountUseCase(totalAmount, currency),
                categoryTransactions = newCategoryTransaction,
                hideValues = categoryTransactions.isEmpty() || !allAvailable,
                categoryType = categoryType,
            )
        }.flowOn(appCoroutineDispatchers.computation)
    }
}

fun CategoryTransaction.toChartModel(): PieChartData {
    return PieChartData(
        name = this.category.name,
        value = this.percent,
        color = this.category.storedIcon.backgroundColor,
        titleResId = this.category.titleResId,
    )
}
