package com.dolphin.expenseease.utils.google

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import java.util.Collections
import javax.inject.Inject
import kotlinx.coroutines.withContext
import com.google.api.services.sheets.v4.model.*

class SheetsServiceHelper @Inject constructor(
    context: Context,
    account: GoogleSignInAccount
) {
    private val sheetsService: Sheets by lazy {
        Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(SheetsScopes.SPREADSHEETS)
            ).apply {
                selectedAccount = account.account
            }
        ).setApplicationName("Budget App")
            .build()
    }

    suspend fun createSpreadsheet(title: String): String = withContext(Dispatchers.IO) {
        val spreadsheet = Spreadsheet().apply {
            properties = SpreadsheetProperties().setTitle(title)
        }
        sheetsService.spreadsheets().create(spreadsheet).execute().spreadsheetId
    }

    suspend fun writeData(spreadsheetId: String, range: String, values: List<List<Any>>) = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("SheetsServiceHelper", "Writing ${values.size} rows to $range")
            sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED")
                .execute()
            android.util.Log.i("SheetsServiceHelper", "Successfully wrote to $range")
        } catch (e: Exception) {
            android.util.Log.e("SheetsServiceHelper", "Error writing to $range: ${e.message}", e)
            throw e
        }
    }

    suspend fun appendData(spreadsheetId: String, range: String, values: List<List<Any>>) = withContext(Dispatchers.IO) {
        try {
            android.util.Log.i("SheetsServiceHelper", "Appending ${values.size} rows to $range")
            sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
            android.util.Log.i("SheetsServiceHelper", "Successfully appended to $range")
        } catch (e: Exception) {
            android.util.Log.e("SheetsServiceHelper", "Error appending to $range: ${e.message}", e)
            throw e
        }
    }

    suspend fun readData(spreadsheetId: String, range: String): List<List<Any>> = withContext(Dispatchers.IO) {
        try {
            val result = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
                .values

            // Ensure each row is a List<Any> and handle null or invalid data
            val data = result?.mapNotNull { row ->
                if (row is List<*>) {
                    row.mapNotNull { it as? Any } // Safely cast each element to Any
                } else {
                    null // Skip rows that are not lists
                }
            } ?: emptyList() // Return an empty list if result is null

            android.util.Log.i("SheetsServiceHelper", "Read ${data.size} rows from $range")
            data
        } catch (e: Exception) {
            android.util.Log.e("SheetsServiceHelper", "Error reading data from $range: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun spreadsheetExists(spreadsheetId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            sheetsService.spreadsheets().get(spreadsheetId).execute()
            android.util.Log.i("SheetsServiceHelper", "Spreadsheet $spreadsheetId exists")
            true
        } catch (e: Exception) {
            android.util.Log.w("SheetsServiceHelper", "Spreadsheet $spreadsheetId does not exist or is inaccessible: ${e.message}")
            false
        }
    }

    suspend fun setupSpreadSheet(spreadsheetId: String) = withContext(Dispatchers.IO) {
        val requests = mutableListOf<Request>()

        // Rename the default sheet to "Expenses"
        requests.add(
            Request().setUpdateSheetProperties(
                UpdateSheetPropertiesRequest().apply {
                    properties = SheetProperties().apply {
                        sheetId = 0 // Default sheet ID is usually 0
                        title = "Expenses"
                    }
                    fields = "title"
                }
            )
        )

        // Add a new sheet named "Budgets"
        requests.add(
            Request().setAddSheet(
                AddSheetRequest().apply {
                    properties = SheetProperties().apply {
                        title = "Budgets"
                    }
                }
            )
        )

        // Add a new sheet named "Wallet"
        requests.add(
            Request().setAddSheet(
                AddSheetRequest().apply {
                    properties = SheetProperties().apply {
                        title = "Wallet"
                    }
                }
            )
        )

        // Execute the batch update
        val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(requests)
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
    }
}