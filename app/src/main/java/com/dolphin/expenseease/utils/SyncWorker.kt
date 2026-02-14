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

        // Check if spreadsheet exists, if not create a new one
        if (spreadsheetId != null) {
            val exists = sheetsServiceHelper.spreadsheetExists(spreadsheetId)
            if (!exists) {
                Log.i("SyncWorker", "Spreadsheet $spreadsheetId was deleted, creating new one")
                spreadsheetId = null // Force creation of new sheet
            }
        }

        // Create spreadsheet if it doesn't exist
        if (spreadsheetId == null) {
            Log.i("SyncWorker", "Creating new spreadsheet: ${getCurrentYearSheetName()}")
            spreadsheetId = sheetsServiceHelper.createSpreadsheet(getCurrentYearSheetName())
            sheetsServiceHelper.setupSpreadSheet(spreadsheetId)
            Log.i("SyncWorker", "Created new spreadsheet with ID: $spreadsheetId")
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
        Log.i("SyncWorker", "Starting sync for $sheetName with ${newData.size} records")

        // Read existing data
        val existingData = sheetsServiceHelper.readData(spreadsheetId, "$sheetName!A:Z")

        Log.i("SyncWorker", "$sheetName: Read ${existingData.size} existing rows (including header)")

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
            Log.i("SyncWorker", "Created new $sheetName sheet with ${newData.size} rows")
        } else {
            // Build a map of existing IDs to row numbers
            val existingIds = mutableMapOf<String, Int>()
            existingData.forEachIndexed { index, row ->
                if (index > 0 && row.isNotEmpty()) { // Skip header row
                    val id = row.getOrNull(idColumnIndex)?.toString()
                    if (id != null && id.isNotEmpty()) {
                        existingIds[id] = index + 1 // 1-based row number
                    }
                }
            }

            Log.i("SyncWorker", "$sheetName: Found ${existingIds.size} existing records")

            // Separate data into updates and new additions
            val rowsToUpdate = mutableListOf<Pair<Int, List<Any>>>()
            val rowsToAppend = mutableListOf<List<Any>>()

            newData.forEach { row ->
                val id = row.getOrNull(idColumnIndex)?.toString()
                if (id != null && id.isNotEmpty()) {
                    val existingRowNumber = existingIds[id]
                    if (existingRowNumber != null) {
                        // Will update existing row
                        rowsToUpdate.add(Pair(existingRowNumber, row))
                    } else {
                        // Will append new row
                        rowsToAppend.add(row)
                    }
                }
            }

            Log.i("SyncWorker", "$sheetName: Will update ${rowsToUpdate.size} rows, append ${rowsToAppend.size} rows")

            // Update existing rows one by one
            rowsToUpdate.forEach { (rowNumber, row) ->
                val range = "$sheetName!A$rowNumber"
                sheetsServiceHelper.writeData(spreadsheetId, range, listOf(row))
            }

            // Append all new rows at once
            if (rowsToAppend.isNotEmpty()) {
                sheetsServiceHelper.appendData(spreadsheetId, "$sheetName!A:A", rowsToAppend)
                Log.i("SyncWorker", "Appended ${rowsToAppend.size} new rows to $sheetName")
            }

            Log.i("SyncWorker", "$sheetName sync complete: Updated ${rowsToUpdate.size}, Appended ${rowsToAppend.size}")
        }
    }
}

