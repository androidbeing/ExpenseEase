package com.dolphin.expenseease.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.AppDatabase
import com.dolphin.expenseease.databinding.ActivityMainBinding
import com.dolphin.expenseease.utils.Constants.EMAIL_ID
import com.dolphin.expenseease.utils.Constants.USER_NAME
import com.dolphin.expenseease.utils.GoogleSpreadSheetHelper
import com.dolphin.expenseease.utils.PreferenceHelper
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private val RC_SIGN_IN = 500

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_budget,
                R.id.nav_expenses,
                R.id.nav_wallet,
                R.id.nav_reminders,
                R.id.nav_backup,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initNavHeader()
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