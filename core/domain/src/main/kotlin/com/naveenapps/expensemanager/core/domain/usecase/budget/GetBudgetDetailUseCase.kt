package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBudgetDetailUseCase(
    private val budgetRepository: BudgetRepository,
    private val calculateBudgetUseCase: CalculateBudgetUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {
    operator fun invoke(budgetId: String): Flow<BudgetUiModel?> {
        return combine(
            budgetRepository.findBudgetByIdFlow(budgetId),
            getTransactionWithFilterUseCase.invoke(),
        ) { budget, _ ->
            budget?.let { calculateBudgetUseCase(it) }
        }
    }
}
