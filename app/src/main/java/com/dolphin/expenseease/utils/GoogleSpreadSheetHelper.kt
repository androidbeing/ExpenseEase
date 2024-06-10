package com.dolphin.expenseease.utils

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import com.dolphin.expenseease.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteSheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object GoogleSpreadSheetHelper {
    fun syncToSpreadSheet(context: Context, data: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val googleSignInAccount = task.getResult(ApiException::class.java)
            val email = googleSignInAccount?.email ?: throw Exception("Email not found")

            // Get the AccountManager
            val accountManager = AccountManager.get(context)

            // Find the Account object matching the email
            val accounts = accountManager.getAccountsByType("com.google")
            val account = accounts.find { it.name == email }
                ?: throw Exception("Account not found for email: $email")

            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf("https://www.googleapis.com/auth/spreadsheets")
            )
            credential.selectedAccount = account // Now you can assign the Account object

            val service = Sheets.Builder(
                NetHttpTransport(), GsonFactory(), credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
            CoroutineScope(Dispatchers.IO).launch {
                // 1. Create the Spreadsheet (if it doesn't already exist)
                val spreadsheet = Spreadsheet()
                    .setProperties(SpreadsheetProperties().setTitle("ExpenseEaseSheet"))

                val createdSpreadsheet = service.spreadsheets().create(spreadsheet)
                    .execute()

                val newSpreadsheetId = createdSpreadsheet.spreadsheetId
                println("New spreadsheet created with ID: $newSpreadsheetId")

                // 2. Determine the Current Month and Generate Sheet Name
                val calendar = Calendar.getInstance()
                val sheetName = "${
                    SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                    ).format(calendar.time)
                }"

                // 3. Check if the sheet for the current month already exists
                val spreadsheetProperties = service.spreadsheets().get(newSpreadsheetId)
                    .setIncludeGridData(false) // We only need sheet properties
                    .execute()
                val sheetExists =
                    spreadsheetProperties.sheets.any { it.properties.title == sheetName }

                // 4. Create the new sheet if it doesn't exist
                if (!sheetExists) {
                    val addSheetRequest = AddSheetRequest().setProperties(
                        SheetProperties().setTitle(sheetName)
                    )
                    val batchUpdateRequestForAdd =
                        BatchUpdateSpreadsheetRequest().setRequests(
                            listOf(Request().setAddSheet(addSheetRequest))
                        )
                    service.spreadsheets()
                        .batchUpdate(newSpreadsheetId, batchUpdateRequestForAdd).execute()
                    println("New sheet '$sheetName' created.")
                } else {
                    println("Sheet '$sheetName' already exists.")
                }

                // 5. Delete the default "Sheet1"
                val defaultSheetExists =
                    spreadsheetProperties.sheets.any { it.properties.title == "Sheet1" }
                if (defaultSheetExists) {
                    val deleteSheetRequest =
                        DeleteSheetRequest().setSheetId(0) // Sheet1 has ID 0
                    val batchUpdateRequestForDelete =
                        BatchUpdateSpreadsheetRequest().setRequests(
                            listOf(Request().setDeleteSheet(deleteSheetRequest))
                        )
                    service.spreadsheets()
                        .batchUpdate(newSpreadsheetId, batchUpdateRequestForDelete)
                        .execute()
                    println("Default 'Sheet1' deleted.")
                }

                // 6. Write Data to the Correct Sheet
                val values = listOf(
                    listOf("Item", "Price", "Quantity"),
                    listOf("Product A", 10.99, 5),
                    listOf("Product B", 5.49, 10)
                )
                val body = ValueRange().setValues(values)

                val range = "$sheetName!A1:C3" // Use the generated sheet name

                val result = service.spreadsheets().values()
                    .update(newSpreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute()

                println("Cells updated in sheet '$sheetName': ${result.updatedCells}")
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            // Handle Google Sign-In errors
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle other exceptions (e.g., email or account not found)
        }
    }
}