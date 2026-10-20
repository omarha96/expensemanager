package com.naveenapps.expensemanager.feature.settings.di

import com.naveenapps.expensemanager.feature.settings.SettingsViewModel
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsViewModel
import com.naveenapps.expensemanager.feature.settings.currencyapi.CurrencyApiProfileViewModel
import com.naveenapps.expensemanager.feature.settings.exchangemethod.ExchangeMethodViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val SettingsViewModelModule = module {
    viewModel {
        SettingsViewModel(
            getDefaultCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            getCurrentThemeUseCase = get(),
            getCurrentLocaleUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            settingsRepository = get(),
            appComposeNavigator = get(),
            analyticsRepository = get(),
            backupRepository = get(),
            setRestoreCurrencyUseCase = get(),
        )
    }
    viewModel {
        AdvancedSettingsViewModel(
            appComposeNavigator = get(),
        )
    }
    viewModel {
        ExchangeMethodViewModel(
            exchangeRateRepository = get(),
            saveStaticExchangeRateUseCase = get(),
            getCurrencyApiProfileUseCase = get(),
            getAllAccountsUseCase = get(),
            getCurrencyUseCase = get(),
            appComposeNavigator = get(),
        )
    }
    viewModel {
        CurrencyApiProfileViewModel(
            getCurrencyApiProfileUseCase = get(),
            saveCurrencyApiProfileUseCase = get(),
            appComposeNavigator = get(),
        )
    }
}
