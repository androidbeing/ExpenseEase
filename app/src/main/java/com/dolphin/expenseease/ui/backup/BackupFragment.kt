package com.dolphin.expenseease.ui.backup

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.utils.ToastUtils

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
        emailId = PreferenceHelper.getString(EMAIL_ID) ?: ""
        lastSyncTimeMillis = PreferenceHelper.getLong(LAST_SYNC_ON)
        val lastSyncTime =
            if (lastSyncTimeMillis > 0) requireActivity().getRelativeTimeString(lastSyncTimeMillis) else getString(
                R.string.not_synced
            )
        binding.valEmailId.text = emailId
        binding.valUserName.text = PreferenceHelper.getString(USER_NAME)
        binding.valLastSyncTime.text = lastSyncTime

        binding.btnSync.setOnClickListener {
            checkAndSyncData()
        }
    }

    private fun initGoogle() {
        googleSignInHelper = GoogleSignInHelper(
            requireActivity(), object : GoogleSignInHelper.GoogleSignInCallback {
                override fun onSuccess(account: GoogleSignInAccount) {
                    PreferenceHelper.putString(USER_NAME, "${account.displayName}")
                    PreferenceHelper.putString(EMAIL_ID, "${account.email}")
                    Log.i("AAA", "Google Auth Success: ${account.email}")
                    lifecycleScope.launch(Dispatchers.IO) {
                        sheetsServiceHelper = SheetsServiceHelper(requireActivity(), account)
                        withContext(Dispatchers.Main) {
                            exportDataToSheets()
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.i("AAA", "Sign-in failed: ${exception.message}")
                }
            }
        )
    }

    private fun exportDataToSheets() {
        binding.progressBar.visibility = View.VISIBLE
        var spreadsheetId = PreferenceHelper.getString(SPREAD_SHEET_ID, null)
        lifecycleScope.launch {
            try {
                // Prepare data for Expenses
                val expenseValues = mutableListOf<List<Any>>().apply {
                    if (spreadsheetId == null) {
                        add(listOf("Date", "Type", "Amount", "Notes", "CreatedAt", "UpdatedAt"))
                    }
                    addAll(expenseList.map { expense ->
                        listOf(
                            expense.date,
                            expense.type,
                            expense.amount,
                            expense.notes,
                            expense.createdAt,
                            expense.updatedAt
                        )
                    })
                }

                // Prepare data for Budgets
                val budgetValues = mutableListOf<List<Any>>().apply {
                    if (spreadsheetId == null) {
                        add(listOf("Type", "Amount", "MonthYear", "CreatedAt", "UpdatedAt"))
                    }
                    addAll(budgetList.map { budget ->
                        listOf(
                            budget.type,
                            budget.amount,
                            budget.monthYear,
                            budget.createdAt,
                            budget.updatedAt
                        )
                    })
                }

                // Prepare data for Wallets
                val walletValues = mutableListOf<List<Any>>().apply {
                    if (spreadsheetId == null) {
                        add(listOf("Balance", "AddedAmount", "Notes", "CreatedAt", "UpdatedAt"))
                    }
                    addAll(walletList.map { wallet ->
                        listOf(
                            wallet.balance,
                            wallet.addedAmount,
                            wallet.notes,
                            wallet.createdAt,
                            wallet.updatedAt
                        )
                    })
                }

                // These will automatically use IO dispatcher from SheetsServiceHelper
                if (spreadsheetId == null) {
                    spreadsheetId =
                        sheetsServiceHelper.createSpreadsheet("${getCurrentYearSheetName()}")
                    sheetsServiceHelper.setupSpreadSheet(spreadsheetId)
                }

                // Write data to Expenses sheet
                val existingExpenseData =
                    sheetsServiceHelper.readData(spreadsheetId, "Expenses!A1:F") ?: emptyList()
                val nextExpenseRow =
                    if (existingExpenseData.isNotEmpty() && existingExpenseData is List<*>) {
                        existingExpenseData[0].size + 1
                    } else {
                        1 // Start from the first row if no data exists
                    }
                sheetsServiceHelper.writeData(
                    spreadsheetId,
                    "Expenses!A$nextExpenseRow:F${nextExpenseRow + expenseValues.size - 1}",
                    expenseValues
                )

                // Write data to Budgets sheet
                val existingBudgetData =
                    sheetsServiceHelper.readData(spreadsheetId, "Budgets!A1:E") ?: emptyList()
                val nextBudgetRow =
                    if (existingBudgetData.isNotEmpty() && existingBudgetData is List<*>) {
                        existingBudgetData[0].size + 1
                    } else {
                        1 // Start from the first row if no data exists
                    }
                sheetsServiceHelper.writeData(
                    spreadsheetId,
                    "Budgets!A$nextBudgetRow:E${nextBudgetRow + budgetValues.size - 1}",
                    budgetValues
                )

                // Write data to Wallet sheet
                val existingWalletData =
                    sheetsServiceHelper.readData(spreadsheetId, "Wallet!A1:E") ?: emptyList()
                val nextWalletRow =
                    if (existingWalletData.isNotEmpty() && existingWalletData is List<*>) {
                        existingWalletData[0].size + 1
                    } else {
                        1 // Start from the first row if no data exists
                    }
                sheetsServiceHelper.writeData(
                    spreadsheetId,
                    "Wallet!A$nextWalletRow:E${nextWalletRow + walletValues.size - 1}",
                    walletValues
                )

                Log.i("AAA", "Data written successfully to spreadsheet")
                val spreadsheetUrl = "https://docs.google.com/spreadsheets/d/$spreadsheetId"
                PreferenceHelper.putString(SPREAD_SHEET_ID, spreadsheetId)
                PreferenceHelper.putString(SPREAD_SHEET_URL, spreadsheetUrl)
                PreferenceHelper.putLong(LAST_SYNC_ON, System.currentTimeMillis())
                viewModel.addSheet(
                    MySheet(
                        sheetName = spreadsheetId,
                        sheetLink = spreadsheetUrl,
                        email = emailId
                    )
                )

                binding.btnSync.isEnabled = false
                binding.progressBar.visibility = View.GONE
                ToastUtils.showLong(requireActivity(), getString(R.string.sync_success))
                Log.i("AAA", "$spreadsheetUrl")
            } catch (e: Exception) {
                ToastUtils.showLong(requireActivity(), getString(R.string.sync_fail))
                binding.progressBar.visibility = View.GONE
                Log.i("AAA", "Error during export: ${e.message}")
                Log.e("SheetsExport", "Export failed", e)
            }
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