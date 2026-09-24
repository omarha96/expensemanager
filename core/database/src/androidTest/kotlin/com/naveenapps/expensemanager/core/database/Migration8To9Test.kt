package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration_test"

@RunWith(AndroidJUnit4::class)
class Migration8To9Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    // region schema shape

    @Test
    @Throws(IOException::class)
    fun afterMigration_accountTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 8).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val cursor = db.query("PRAGMA table_info(account)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("currency_code")
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_transactionTableHasNewColumn() {
        helper.createDatabase(TEST_DB, 8).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val cursor = db.query("PRAGMA table_info(`transaction`)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).contains("currency_code")
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_exchangeRatesTableExists() {
        helper.createDatabase(TEST_DB, 8).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

        val cursor = db.query("PRAGMA table_info(exchange_rates)")
        val columnNames = buildList {
            while (cursor.moveToNext()) {
                add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        db.close()

        assertThat(columnNames).containsExactly("base_code", "target_code", "rate", "fetched_at")
    }

    // endregion

    // region backfill for existing installs

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingAccountsHaveEmptyCurrencyCode() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        db8.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent()
        )
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        val cursor = db9.query("SELECT currency_code FROM account WHERE id = 'acc-1'")

        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("currency_code"))).isEqualTo("")

        cursor.close()
        db9.close()
    }

    @Test
    @Throws(IOException::class)
    fun afterMigration_existingTransactionsHaveEmptyCurrencyCode() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        db8.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on)
            VALUES
                ('acc-1', 'Checking', 0, '#FFFFFF', 'ic_bank', 1000.0, 0.0, 1, 1000, 2000)
            """.trimIndent()
        )
        db8.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id)
            VALUES
                ('txn-1', 'Groceries', 'cat-1', 'acc-1', 0, 50.0, '', 1000, 2000, NULL)
            """.trimIndent()
        )
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        val cursor = db9.query("SELECT currency_code FROM `transaction` WHERE id = 'txn-1'")

        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("currency_code"))).isEqualTo("")

        cursor.close()
        db9.close()
    }

    // endregion

    // region other tables unaffected

    @Test
    @Throws(IOException::class)
    fun afterMigration_otherTablesAreUntouched() {
        val db8 = helper.createDatabase(TEST_DB, 8)
        db8.execSQL(
            """
            INSERT INTO budget
                (id, selected_month, amount, all_accounts_selected, all_categories_selected,
                 created_on, updated_on, period_type)
            VALUES
                ('budget-1', '2024-01', 500.0, 1, 1, 1000, 2000, 0)
            """.trimIndent()
        )
        db8.close()

        val db9 = helper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)
        val cursor = db9.query("SELECT id, amount FROM budget")
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("id"))).isEqualTo("budget-1")
        assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))).isEqualTo(500.0)
        cursor.close()
        db9.close()
    }

    // endregion
}
