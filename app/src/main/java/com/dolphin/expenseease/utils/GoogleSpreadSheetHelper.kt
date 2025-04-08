package com.dolphin.expenseease.utils

import android.content.Context
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object GoogleSpreadSheetHelper {

    fun syncDataToSpreadSheet(
        context: Context,
        expenses: List<Expense>,
        budgets: List<Budget>,
        wallets: List<MyWallet>,
        reminders: List<Reminder>
    ) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf("https://www.googleapis.com/auth/spreadsheets")
        )
        val service = Sheets.Builder(
            NetHttpTransport(), GsonFactory(), credential
        )
            .setApplicationName(context.getString(R.string.app_name))
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val spreadsheetId = createOrGetSpreadsheet(service)
            val sheetName = getCurrentMonthSheetName()

            createSheetIfNotExists(service, spreadsheetId, sheetName)
            syncTableData(service, spreadsheetId, sheetName, "Expenses", expenses)
            syncTableData(service, spreadsheetId, sheetName, "Budgets", budgets)
            syncTableData(service, spreadsheetId, sheetName, "Wallets", wallets)
            syncTableData(service, spreadsheetId, sheetName, "Reminders", reminders)
        }
    }

    private fun createOrGetSpreadsheet(service: Sheets): String {
        val spreadsheet = Spreadsheet().setProperties(SpreadsheetProperties().setTitle("ExpenseEaseSheet"))
        val createdSpreadsheet = service.spreadsheets().create(spreadsheet).execute()
        return createdSpreadsheet.spreadsheetId
    }

    private fun getCurrentMonthSheetName(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    private fun createSheetIfNotExists(service: Sheets, spreadsheetId: String, sheetName: String) {
        val spreadsheetProperties = service.spreadsheets().get(spreadsheetId).setIncludeGridData(false).execute()
        val sheetExists = spreadsheetProperties.sheets.any { it.properties.title == sheetName }

        if (!sheetExists) {
            val addSheetRequest = AddSheetRequest().setProperties(SheetProperties().setTitle(sheetName))
            val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(listOf(Request().setAddSheet(addSheetRequest)))
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
        }
    }

    private fun <T> syncTableData(service: Sheets, spreadsheetId: String, sheetName: String, tableName: String, data: List<T>) {
        val values = mutableListOf<List<Any>>()
        values.add(data.first()!!::class.java.declaredFields.map { it.name }) // Add column names
        data.forEach { item ->
            values.add(item!!::class.java.declaredFields.map { field ->
                field.isAccessible = true
                field.get(item) ?: ""
            })
        }

        val body = ValueRange().setValues(values)
        val range = "$sheetName!$tableName!A1:${'A' + values.first().size - 1}${values.size}"
        service.spreadsheets().values().update(spreadsheetId, range, body).setValueInputOption("RAW").execute()
    }
}