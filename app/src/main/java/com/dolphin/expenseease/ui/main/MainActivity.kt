package com.dolphin.expenseease.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.AppDatabase
import com.dolphin.expenseease.databinding.ActivityMainBinding
import com.dolphin.expenseease.utils.AppUpdateHelper
import com.dolphin.expenseease.utils.Constants.EMAIL_ID
import com.dolphin.expenseease.utils.Constants.USER_NAME
import com.dolphin.expenseease.utils.GoogleSpreadSheetHelper
import com.dolphin.expenseease.utils.PreferenceHelper
import com.dolphin.expenseease.utils.SyncWorker
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdateHelper: AppUpdateHelper

    private val RC_SIGN_IN = 500

    // Activity result launcher for In-App Updates
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Log.w("MainActivity", "Update flow failed! Result code: ${result.resultCode}")
            }
        }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getInstance(applicationContext)
                            val expenses = db.expenseDao().getAll()
                            val budgets = db.budgetDao().getAll()
                            val wallets = db.myWalletDao().getAll()
                            val reminders = db.reminderDao().getAll()

                            GoogleSpreadSheetHelper.syncDataToSpreadSheet(
                                applicationContext,
                                expenses.value!!,
                                budgets.value!!,
                                wallets.value!!,
                                reminders.value!!
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                //syncToSpreadSheet(applicationContext, data!!)
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.i("AAA", "Result Cancelled...")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // ...existing code...
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_expenses,
                R.id.nav_budget,
                R.id.nav_wallet,
                R.id.nav_reminders,
                R.id.nav_backup,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initNavHeader()
        setupAutoSync()
        setupInAppUpdate()
    }

    private fun setupInAppUpdate() {
        // Initialize app update helper
        appUpdateHelper = AppUpdateHelper(this)

        // Set up callbacks
        appUpdateHelper.onDownloadComplete = {
            showUpdateDownloadedSnackbar()
        }

        appUpdateHelper.onUpdateFailed = {
            Log.e("MainActivity", "App update failed")
        }

        appUpdateHelper.onUpdateCanceled = {
            Log.w("MainActivity", "App update canceled by user")
        }

        // Check for updates
        appUpdateHelper.checkForUpdates(updateLauncher)
    }

    private fun showUpdateDownloadedSnackbar() {
        Snackbar.make(
            binding.root,
            "An update has been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("INSTALL") {
                appUpdateHelper.completeUpdate()
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if update is in progress when app is resumed
        if (::appUpdateHelper.isInitialized) {
            appUpdateHelper.checkUpdateOnResume(updateLauncher)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup update helper
        if (::appUpdateHelper.isInitialized) {
            appUpdateHelper.cleanup()
        }
    }

    private fun setupAutoSync() {
        // Setup periodic work request for auto-sync every 12 hours
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            12, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AutoSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Log.i("MainActivity", "Auto-sync scheduled: Every 12 hours")
    }

    /*private fun syncInSheets() {
        val signInClient = getGoogleClient()
        startForResult.launch(signInClient.signInIntent)
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        //return true
        return super.onCreateOptionsMenu(menu)
    }

    private fun initNavHeader() {
        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0) // Get the first header view

        val userNameTextView = headerView.findViewById<TextView>(R.id.txtName)
        val emailTextView = headerView.findViewById<TextView>(R.id.txtEmail)

        userNameTextView.text = PreferenceHelper.getString(USER_NAME) ?: getString(R.string.app_name) // Set the user name
        emailTextView.text = PreferenceHelper.getString(EMAIL_ID) ?: "" // Set the email
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /*private fun getGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(GOOGLE_AUTH_URL)) // Add required scopes
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        return googleSignInClient
    }*/
}