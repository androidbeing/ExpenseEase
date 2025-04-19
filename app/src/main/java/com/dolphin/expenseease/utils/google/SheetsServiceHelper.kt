package com.dolphin.expenseease.utils.google

import android.content.Context
import android.util.Log
import com.dolphin.expenseease.data.db.expense.Expense
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
import kotlin.text.get

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
        sheetsService.spreadsheets().values()
            .update(spreadsheetId, range, ValueRange().setValues(values))
            .setValueInputOption("RAW")
            .execute()
    }

    suspend fun readData(spreadsheetId: String, range: String): List<List<Any>> = withContext(Dispatchers.IO) {
        val result = sheetsService.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
            .values

        // Ensure each row is a List<Any> and handle null or invalid data
        result?.mapNotNull { row ->
            if (row is List<*>) {
                row.mapNotNull { it as? Any } // Safely cast each element to Any
            } else {
                null // Skip rows that are not lists
            }
        } ?: emptyList() // Return an empty list if result is null
    }
}