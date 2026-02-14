package com.dolphin.expenseease.utils

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dolphin.expenseease.data.db.AppDatabase
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.utils.Constants.EMAIL_ID
import com.dolphin.expenseease.utils.Constants.LAST_SYNC_ON
import com.dolphin.expenseease.utils.Constants.SPREAD_SHEET_ID
import com.dolphin.expenseease.utils.Constants.SPREAD_SHEET_URL
import com.dolphin.expenseease.utils.google.SheetUtils.getCurrentYearSheetName
import com.dolphin.expenseease.utils.google.SheetsServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user is signed in to Google
                val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
                if (account == null) {
                    Log.i("SyncWorker", "User not signed in, skipping sync")
                    return@withContext Result.success()
                }

                val db = AppDatabase.getInstance(applicationContext)
                val lastSyncTimeMillis = PreferenceHelper.getLong(LAST_SYNC_ON)

                // Get all data that needs to be synced (created/updated after last sync)
                val allExpenses = db.expenseDao().getAll().value ?: emptyList()
                val allBudgets = db.budgetDao().getAll().value ?: emptyList()
                val allWallets = db.myWalletDao().getAll().value ?: emptyList()

                // Filter only new/updated items since last sync
                val expensesToSync = allExpenses.filter { it.createdAt > lastSyncTimeMillis || it.updatedAt > lastSyncTimeMillis }
                val budgetsToSync = allBudgets.filter { it.createdAt > lastSyncTimeMillis || it.updatedAt > lastSyncTimeMillis }
                val walletsToSync = allWallets.filter { it.createdAt > lastSyncTimeMillis || it.updatedAt > lastSyncTimeMillis }

                // If nothing to sync, return success
                if (expensesToSync.isEmpty() && budgetsToSync.isEmpty() && walletsToSync.isEmpty()) {
                    Log.i("SyncWorker", "No new data to sync")
                    return@withContext Result.success()
                }

                // Initialize sheets service helper
                val sheetsServiceHelper = SheetsServiceHelper(applicationContext, account)

                // Sync data
                syncDataToSheets(
                    sheetsServiceHelper,
                    expensesToSync,
                    budgetsToSync,
                    walletsToSync,
                    db
                )

                Log.i("SyncWorker", "Auto-sync completed successfully")
                Result.success()
            } catch (e: Exception) {
                Log.e("SyncWorker", "Auto-sync failed: ${e.message}", e)
                Result.retry()
            }
        }
    }

    private suspend fun syncDataToSheets(
        sheetsServiceHelper: SheetsServiceHelper,
        expenses: List<Expense>,
        budgets: List<Budget>,
        wallets: List<MyWallet>,
        db: AppDatabase
    ) {
        var spreadsheetId = PreferenceHelper.getString(SPREAD_SHEET_ID, null)

        // Check if spreadsheet exists, if not, create a new one
        if (spreadsheetId != null) {
            try {
                val exists = sheetsServiceHelper.spreadsheetExists(spreadsheetId)
                if (!exists) {
                    Log.i("SyncWorker", "Spreadsheet $spreadsheetId was deleted, creating new one")
                    spreadsheetId = null
                }
            } catch (e: Exception) {
                // If we can't verify existence (network/auth issues), assume it exists and let the sync fail naturally
                Log.w("SyncWorker", "Could not verify spreadsheet existence, proceeding with sync: ${e.message}")
            }
        }

        // Create spreadsheet if it doesn't exist
        if (spreadsheetId == null) {
            spreadsheetId = sheetsServiceHelper.createSpreadsheet(getCurrentYearSheetName())
            sheetsServiceHelper.setupSpreadSheet(spreadsheetId)
        }

        // Prepare and write expense data
        if (expenses.isNotEmpty()) {
            val expenseValues = expenses.map { expense ->
                listOf(
                    expense.id,
                    expense.date,
                    expense.type,
                    expense.amount,
                    expense.notes,
                    expense.createdAt,
                    expense.updatedAt
                )
            }
            appendOrUpdateData(sheetsServiceHelper, spreadsheetId, "Expenses", expenseValues, 0)
        }

        // Prepare and write budget data
        if (budgets.isNotEmpty()) {
            val budgetValues = budgets.map { budget ->
                listOf(
                    budget.id,
                    budget.type,
                    budget.amount,
                    budget.monthYear,
                    budget.createdAt,
                    budget.updatedAt
                )
            }
            appendOrUpdateData(sheetsServiceHelper, spreadsheetId, "Budgets", budgetValues, 0)
        }

        // Prepare and write wallet data
        if (wallets.isNotEmpty()) {
            val walletValues = wallets.map { wallet ->
                listOf(
                    wallet.id,
                    wallet.balance,
                    wallet.addedAmount,
                    wallet.notes,
                    wallet.createdAt,
                    wallet.updatedAt
                )
            }
            appendOrUpdateData(sheetsServiceHelper, spreadsheetId, "Wallet", walletValues, 0)
        }

        // Update last sync time and save spreadsheet info
        val currentTime = System.currentTimeMillis()
        PreferenceHelper.putLong(LAST_SYNC_ON, currentTime)
        PreferenceHelper.putString(SPREAD_SHEET_ID, spreadsheetId)

        val spreadsheetUrl = "https://docs.google.com/spreadsheets/d/$spreadsheetId"
        PreferenceHelper.putString(SPREAD_SHEET_URL, spreadsheetUrl)

        val emailId = PreferenceHelper.getString(EMAIL_ID) ?: ""
        db.sheetDao().insert(
            MySheet(
                sheetName = spreadsheetId,
                sheetLink = spreadsheetUrl,
                email = emailId
            )
        )
    }

    private suspend fun appendOrUpdateData(
        sheetsServiceHelper: SheetsServiceHelper,
        spreadsheetId: String,
        sheetName: String,
        newData: List<List<Any>>,
        idColumnIndex: Int
    ) {
        // Read existing data
        val existingData = sheetsServiceHelper.readData(spreadsheetId, "$sheetName!A:Z") ?: emptyList()

        if (existingData.isEmpty()) {
            // No existing data, write headers and data
            val headers = when (sheetName) {
                "Expenses" -> listOf(listOf("ID", "Date", "Type", "Amount", "Notes", "CreatedAt", "UpdatedAt"))
                "Budgets" -> listOf(listOf("ID", "Type", "Amount", "MonthYear", "CreatedAt", "UpdatedAt"))
                "Wallet" -> listOf(listOf("ID", "Balance", "AddedAmount", "Notes", "CreatedAt", "UpdatedAt"))
                else -> emptyList()
            }
            val allData = headers + newData
            sheetsServiceHelper.writeData(spreadsheetId, "$sheetName!A1", allData)
        } else {
            // Build a map of existing IDs to row numbers
            val existingIds = mutableMapOf<Any, Int>()
            existingData.forEachIndexed { index, row ->
                if (index > 0 && row.isNotEmpty()) { // Skip header row
                    val id = row.getOrNull(idColumnIndex)
                    if (id != null) {
                        existingIds[id] = index + 1 // 1-based row number
                    }
                }
            }

            // Process each new data row
            newData.forEach { row ->
                val id = row.getOrNull(idColumnIndex)
                if (id != null) {
                    val existingRowNumber = existingIds[id]
                    if (existingRowNumber != null) {
                        // Update existing row
                        val range = "$sheetName!A$existingRowNumber"
                        sheetsServiceHelper.writeData(spreadsheetId, range, listOf(row))
                    } else {
                        // Append new row
                        val nextRow = existingData.size + 1
                        val range = "$sheetName!A$nextRow"
                        sheetsServiceHelper.writeData(spreadsheetId, range, listOf(row))
                    }
                }
            }
        }
    }
}

