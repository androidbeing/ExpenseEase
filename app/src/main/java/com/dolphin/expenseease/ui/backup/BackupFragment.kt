package com.dolphin.expenseease.ui.backup

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.databinding.FragmentBackupBinding
import com.dolphin.expenseease.utils.Constants.EMAIL_ID
import com.dolphin.expenseease.utils.Constants.LAST_SYNC_ON
import com.dolphin.expenseease.utils.Constants.SPREAD_SHEET_ID
import com.dolphin.expenseease.utils.Constants.SPREAD_SHEET_URL
import com.dolphin.expenseease.utils.Constants.LBL_NA
import com.dolphin.expenseease.utils.Constants.USER_NAME
import com.dolphin.expenseease.utils.ExtensiveFunctions.getRelativeTimeString
import com.dolphin.expenseease.utils.PreferenceHelper
import com.dolphin.expenseease.utils.google.GoogleSignInHelper
import com.dolphin.expenseease.utils.google.SheetUtils.getCurrentYearSheetName
import com.dolphin.expenseease.utils.google.SheetsServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue
import android.view.View
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.listeners.OnClickAlertListener
import com.dolphin.expenseease.utils.ToastUtils
import com.dolphin.expenseease.utils.DialogUtils.showAlertDialog

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private val viewModel: BackupViewModel by viewModels()
    private var _binding: FragmentBackupBinding? = null
    private lateinit var expenseList: MutableList<Expense>
    private lateinit var budgetList: MutableList<Budget>
    private lateinit var walletList: MutableList<MyWallet>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var sheetsServiceHelper: SheetsServiceHelper
    private var lastSyncTimeMillis: Long = 0
    private var emailId: String = ""

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            googleSignInHelper.handleSignInResult(result.data)
        } else {
            Log.i("AAA", "Sign in failed or was cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseList = mutableListOf()
        budgetList = mutableListOf()
        walletList = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initGoogle()
        initViews()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        viewModel.allExpenses.observe(viewLifecycleOwner) {
            expenseList.clear()
            expenseList.addAll(it.filter { expense -> expense.createdAt > lastSyncTimeMillis })
            updateSyncButtonState()
        }

        viewModel.allBudgets.observe(viewLifecycleOwner) {
            budgetList.clear()
            budgetList.addAll(it.filter { budget -> budget.createdAt > lastSyncTimeMillis })
            updateSyncButtonState()
        }

        viewModel.allWallets.observe(viewLifecycleOwner) {
            walletList.clear()
            walletList.addAll(it.filter { wallet -> wallet.createdAt > lastSyncTimeMillis })
            updateSyncButtonState()
        }
    }

    private fun updateSyncButtonState() {
        binding.btnSync.isEnabled =
            expenseList.isNotEmpty() || budgetList.isNotEmpty() || walletList.isNotEmpty()
    }

    private fun initViews() {
        setLabel()
        binding.btnSync.setOnClickListener {
            checkAndSyncData()
        }
        binding.imgSignout.setOnClickListener {
            googleSignInHelper.signOut()
        }
        binding.textView.setOnClickListener {
            googleSignInHelper.signOut()
        }
    }

    private fun setLabel() {
        emailId = PreferenceHelper.getString(EMAIL_ID) ?: ""
        lastSyncTimeMillis = PreferenceHelper.getLong(LAST_SYNC_ON)
        val lastSyncTime =
            if (lastSyncTimeMillis > 0) requireActivity().getRelativeTimeString(lastSyncTimeMillis) else getString(
                R.string.not_synced
            )
        binding.valEmailId.text = emailId
        binding.valUserName.text = PreferenceHelper.getString(USER_NAME)
        binding.valLastSyncTime.text = lastSyncTime
        binding.groupSignOut.visibility = if(emailId.isNotEmpty() && emailId != "NA") View.VISIBLE else View.GONE
    }

    private fun initGoogle() {
        googleSignInHelper = GoogleSignInHelper(
            requireActivity(), object : GoogleSignInHelper.GoogleSignInCallback {
                override fun onSuccess(account: GoogleSignInAccount) {
                    if (account != null) {
                        PreferenceHelper.putString(USER_NAME, "${account.displayName}")
                        PreferenceHelper.putString(EMAIL_ID, "${account.email}")
                        binding.groupSignOut.visibility = View.VISIBLE
                        Log.i("AAA", "Google Auth Success: ${account.email}")
                        lifecycleScope.launch(Dispatchers.IO) {
                            sheetsServiceHelper = SheetsServiceHelper(requireActivity(), account)
                            withContext(Dispatchers.Main) {
                                exportDataToSheets()
                            }
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.i("AAA", "Sign-in failed: ${exception.message}")
                }

                override fun onSuccess() {
                    binding.groupSignOut.visibility = View.GONE
                    val emailId = PreferenceHelper.getString(EMAIL_ID) ?: ""
                    val alert = Alert(
                        getString(R.string.lbl_signout),
                        getString(R.string.successfully_logout_from, emailId),
                        getString(R.string.okay),
                        LBL_NA
                    )
                    showAlertDialog(requireActivity(), alert, object : OnClickAlertListener {
                        override fun onAcknowledge(isOkay: Boolean) {
                            PreferenceHelper.putString(USER_NAME, "NA")
                            PreferenceHelper.putString(EMAIL_ID, "NA")
                            setLabel()
                        }
                    })
                }
            }
        )
    }

    private fun exportDataToSheets() {
        binding.progressBar.visibility = View.VISIBLE
        var spreadsheetId = PreferenceHelper.getString(SPREAD_SHEET_ID, null)
        lifecycleScope.launch {
            try {
                // Check if spreadsheet exists, if not create a new one
                if (spreadsheetId != null) {
                    val exists = sheetsServiceHelper.spreadsheetExists(spreadsheetId!!)
                    if (!exists) {
                        Log.i("AAA", "Spreadsheet $spreadsheetId was deleted, creating new one")
                        spreadsheetId = null // Force creation of new sheet
                    }
                }

                // Create spreadsheet if it doesn't exist
                if (spreadsheetId == null) {
                    Log.i("AAA", "Creating new spreadsheet: ${getCurrentYearSheetName()}")
                    spreadsheetId = sheetsServiceHelper.createSpreadsheet(getCurrentYearSheetName())
                    sheetsServiceHelper.setupSpreadSheet(spreadsheetId!!)
                    Log.i("AAA", "Created new spreadsheet with ID: $spreadsheetId")
                }

                // At this point, spreadsheetId is guaranteed to be non-null
                val finalSpreadsheetId = spreadsheetId!!

                // Sync Expenses
                if (expenseList.isNotEmpty()) {
                    val expenseValues = expenseList.map { expense ->
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
                    appendOrUpdateData(finalSpreadsheetId, "Expenses", expenseValues, 0)
                }

                // Sync Budgets
                if (budgetList.isNotEmpty()) {
                    val budgetValues = budgetList.map { budget ->
                        listOf(
                            budget.id,
                            budget.type,
                            budget.amount,
                            budget.monthYear,
                            budget.createdAt,
                            budget.updatedAt
                        )
                    }
                    appendOrUpdateData(finalSpreadsheetId, "Budgets", budgetValues, 0)
                }

                // Sync Wallets
                if (walletList.isNotEmpty()) {
                    val walletValues = walletList.map { wallet ->
                        listOf(
                            wallet.id,
                            wallet.balance,
                            wallet.addedAmount,
                            wallet.notes,
                            wallet.createdAt,
                            wallet.updatedAt
                        )
                    }
                    appendOrUpdateData(finalSpreadsheetId, "Wallet", walletValues, 0)
                }

                Log.i("AAA", "Data written successfully to spreadsheet")
                val spreadsheetUrl = "https://docs.google.com/spreadsheets/d/$finalSpreadsheetId"
                PreferenceHelper.putString(SPREAD_SHEET_ID, finalSpreadsheetId)
                PreferenceHelper.putString(SPREAD_SHEET_URL, spreadsheetUrl)
                PreferenceHelper.putLong(LAST_SYNC_ON, System.currentTimeMillis())
                viewModel.addSheet(
                    MySheet(
                        sheetName = finalSpreadsheetId,
                        sheetLink = spreadsheetUrl,
                        email = emailId
                    )
                )

                binding.btnSync.isEnabled = false
                binding.progressBar.visibility = View.GONE
                ToastUtils.showLong(requireActivity(), getString(R.string.sync_success))
                setLabel() // Refresh the UI with new sync time
                Log.i("AAA", spreadsheetUrl)
            } catch (e: Exception) {
                ToastUtils.showLong(requireActivity(), getString(R.string.sync_fail))
                binding.progressBar.visibility = View.GONE
                Log.i("AAA", "Error during export: ${e.message}")
                Log.e("SheetsExport", "Export failed", e)
            }
        }
    }

    private suspend fun appendOrUpdateData(
        spreadsheetId: String,
        sheetName: String,
        newData: List<List<Any>>,
        idColumnIndex: Int
    ) {
        Log.i("AAA", "Starting sync for $sheetName with ${newData.size} records")

        // Read existing data
        val existingData = sheetsServiceHelper.readData(spreadsheetId, "$sheetName!A:Z")

        Log.i("AAA", "$sheetName: Read ${existingData.size} existing rows (including header)")

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
            Log.i("AAA", "Created new $sheetName sheet with ${newData.size} rows")
        } else {
            // Build a map of existing IDs to row numbers
            val existingIds = mutableMapOf<String, Int>()
            existingData.forEachIndexed { index, row ->
                if (index > 0 && row.isNotEmpty()) { // Skip header row
                    val id = row.getOrNull(idColumnIndex)?.toString()
                    if (id != null && id.isNotEmpty()) {
                        existingIds[id] = index + 1 // 1-based row number
                        Log.d("AAA", "$sheetName: Found existing ID=$id at row ${index + 1}")
                    }
                }
            }

            Log.i("AAA", "$sheetName: Found ${existingIds.size} existing records")

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
                        Log.d("AAA", "$sheetName: Will UPDATE ID=$id at row $existingRowNumber")
                    } else {
                        // Will append new row
                        rowsToAppend.add(row)
                        Log.d("AAA", "$sheetName: Will APPEND new ID=$id")
                    }
                }
            }

            Log.i("AAA", "$sheetName: Will update ${rowsToUpdate.size} rows, append ${rowsToAppend.size} rows")

            // Update existing rows one by one
            rowsToUpdate.forEach { (rowNumber, row) ->
                val range = "$sheetName!A$rowNumber"
                sheetsServiceHelper.writeData(spreadsheetId, range, listOf(row))
                Log.i("AAA", "Updated $sheetName row $rowNumber")
            }

            // Append all new rows at once
            if (rowsToAppend.isNotEmpty()) {
                sheetsServiceHelper.appendData(spreadsheetId, "$sheetName!A:A", rowsToAppend)
                Log.i("AAA", "Appended ${rowsToAppend.size} new rows to $sheetName")
            }

            Log.i("AAA", "$sheetName sync complete: Updated ${rowsToUpdate.size}, Appended ${rowsToAppend.size}")
        }
    }

    private fun checkAndSyncData() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            // User is already signed in, proceed with syncing
            Log.i("AAA", "User already signed in: ${account.email}")
            lifecycleScope.launch(Dispatchers.IO) {
                sheetsServiceHelper = SheetsServiceHelper(requireActivity(), account)
                withContext(Dispatchers.Main) {
                    exportDataToSheets()
                }
            }
        } else {
            // User is not signed in, initiate sign-in process
            Log.i("AAA", "User not signed in, initiating sign-in")
            val signInIntent = googleSignInHelper.getSignInIntent()
            signInLauncher.launch(signInIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}