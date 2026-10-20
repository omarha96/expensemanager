package com.naveenapps.expensemanager.feature.budget.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import java.util.Date

@Stable
data class BudgetCreateState(
    val isLoading: Boolean,
    val amount: TextFieldValue<String>,
    val month: TextFieldValue<Date>,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isAllAccountSelected: Boolean,
    val selectedAccounts: List<AccountUiModel>,
    val isAllCategorySelected: Boolean,
    val selectedCategories: List<Category>,
    /** The budget's own currency: the limit is set in it. Locked once the budget exists. */
    val currency: Currency,
    val isCurrencyLocked: Boolean = false,
    val showCurrencySelection: Boolean = false,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
    val showAccountSelectionDialog: Boolean,
    val showCategorySelectionDialog: Boolean,
    val showMonthSelection: Boolean,
)
