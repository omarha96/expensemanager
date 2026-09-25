# Multi-Currency Support — Implementation & Test Plan

Sep 25, 2026 · @Omar

## Overview

Expense Manager (Android, published on Google Play) is single-currency today — one app-wide currency setting applies to every account and transaction. This work adds **per-account currency**, with **central, user-configured API conversion**, so statistics and dashboards stay correct once accounts hold different currencies.

All work lives on the fork branch `claude/project-analysis-claude-md-sdghud` (`omarha96/expensemanager`), across 4 PRs plus follow-up fixes. It has not been merged upstream or to `main` yet.

## Target functionality

| Piece | Behavior |
| --- | --- |
| Account currency | Each account stores its own ISO currency code, set on create/edit via a currency picker |
| Transaction currency | Each transaction freezes the currency of its source account at creation time (never changes retroactively if the account's currency later changes) |
| Exchange-rate source | User-configured API profile (base URL, preset, API key) — no built-in payment/subscription flow |
| Conversion | A single `ConvertAmountUseCase` converts any amount between two currency codes, backed by a local rate cache (1h TTL) with offline fallback to the last-known rate |
| Dashboard | Balance, income/expense totals, and account list convert every amount to the app's selected display currency before summing |
| Statistics / Analysis | Category breakdown, chart data, and averages convert-then-sum (never sum-then-convert), so a total in a mixed-currency period is accurate |
| Statistics viewing-currency switcher | **Planned, not yet built** — a persisted, per-screen override letting the user view statistics in a currency other than the app's global default, with a disclaimer label when a conversion is happening |

## What we implemented

| PR | Scope | Key files |
| --- | --- | --- |
| 1 | New `core:network` module: generic exchange-rate API client with per-provider adapters (Frankfurter, exchangerate.host, Open Exchange Rates, Custom), each owning its own URL/auth/response-parsing. Settings screen to configure the API profile (base URL, preset, key). | `core/network/**`, `feature/settings/.../currencyapi/**` |
| 2 | Room migration 8→9: `currency_code` column on `account` and `transaction`; new `exchange_rates` cache table + DAO. Domain models and mappers updated. | `core/database/.../DatabaseMigrations.kt`, `Migration8To9Test.kt` |
| 3 | Account create/edit screen gets a currency picker (reusing the existing country/currency bottom sheet). New transactions inherit and freeze their source account's currency code. | `feature/account/.../create/**` |
| 4 | `CurrencyApiRepository.getRate` (cache-first, refresh-on-stale, offline fallback) + `ConvertAmountUseCase`, wired into every use case that sums across accounts/transactions: income/expense totals, category breakdown, chart data, averages, and the dashboard view model. | `core/domain/.../usecase/transaction/**`, `feature/dashboard/DashboardViewModel.kt` |
| Fixes | 4 real bugs found and fixed via a fork-local CI workflow (`assembleDebug` + unit tests), none caught locally since the dev sandbox couldn't reach Google's Gradle plugin repo: a `saveProfile` return-type leak, a missing `app → core:network` Gradle dependency, and two test-only mock-stubbing compile errors. | see CI run history on the branch |

Not yet built: the statistics-screen viewing-currency switcher (its own persisted setting, independent of the global default currency) and its disclaimer label.

## Build verification status

A fork-local GitHub Actions workflow (`.github/workflows/verify-branch.yml`, scoped to `claude/**` branches only — never touches the upstream project) proved:

- **`assembleDebug` — green.** Every module compiles, including the new `core:network` module and the DB migration.
- **Unit tests — green.**
- **`spotlessCheck` — red, but pre-existing and unrelated.** The ktlint step crashes on every `.kt` file in the whole repo (including files never touched by this work, e.g. `MainActivity.kt`), which means it would fail identically on a clean checkout of `main`. Likely a ktlint/JDK19 tooling mismatch in the repo's pinned version — out of scope for this feature.

What this build verification does **not** cover: actually running the app. No emulator/device test has been done yet — that's the next section.

## What still needs manual/device testing

None of this has been run on a real device or emulator yet.

- [ ] Install the debug APK (from the `verify-branch.yml` run's `app-debug-apk` artifact) on a phone without errors, app launches
- [ ] Existing single-currency data migrates cleanly: open the app on a pre-existing install (or seeded DB) and confirm accounts/balances/transactions look unchanged after the 8→9 migration
- [ ] Settings → Currency API Profile: enter a base URL + preset (e.g. Frankfurter, no key needed) and save
- [ ] Create a new account with a non-default currency via the picker; confirm it saves and displays correctly
- [ ] Add a transaction against that account; confirm it's tagged with the account's currency
- [ ] Edit an existing account's currency; confirm past transactions keep their original (frozen) currency, not the new one
- [ ] Dashboard: with accounts in 2+ currencies, confirm the balance/income/expense totals are converted and sum correctly (spot-check the math against the configured API's live rate)
- [ ] Analysis screen: category breakdown, chart, and averages reflect converted amounts, not raw mixed-currency sums
- [ ] Turn off network / clear the app's network access: confirm conversion falls back to the last cached rate rather than crashing or showing garbage
- [ ] No currency API profile configured: confirm the app still works (accounts/transactions save fine), and conversions just no-op rather than failing loudly

## Known gaps and deferred work

- **Statistics viewing-currency switcher** — not built. Planned: a persisted per-screen currency override on the Analysis summary card, with a small switch control and a disclaimer label when the shown total isn't in an account's original currency.
- **`spotlessCheck` ktlint failure** — pre-existing, repo-wide, unrelated to this work (see Build verification status above). Not something this feature should fix.
- **Not merged anywhere** — this branch hasn't been merged to `main` on the fork, and no PR has been opened against the upstream project yet. The fork-local `verify-branch.yml` workflow and its "add fork CI" commit are intentionally excluded from any upstream PR.
