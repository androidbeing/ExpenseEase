package com.dolphin.expenseease

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.ActivityMainBinding
import com.dolphin.expenseease.listeners.AddExpenseListener
import com.dolphin.expenseease.ui.home.AddExpenseSheet
import com.dolphin.expenseease.ui.home.HomeViewModel
import com.dolphin.expenseease.utils.ApiEndpoint.GOOGLE_AUTH_URL
import com.dolphin.expenseease.utils.GoogleSpreadSheetHelper.syncToSpreadSheet
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val RC_SIGN_IN = 500

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {
                syncToSpreadSheet(applicationContext, data!!)
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i("AAA", "Result Cancelled...")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            //val signInClient = getGoogleClient()
            //startForResult.launch(signInClient.signInIntent)
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()*/

            val addExpenseBottomSheet = AddExpenseSheet(object : AddExpenseListener {
                override fun onExpenseAdd(expense: Expense) {
                    viewModel.addExpense(expense)
                }
            })
            addExpenseBottomSheet.show(supportFragmentManager, AddExpenseSheet.TAG)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_expenses,
                R.id.nav_wallet,
                R.id.nav_budget,
                R.id.nav_reports,
                R.id.nav_reminders,
                R.id.nav_backup
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        //return true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(GOOGLE_AUTH_URL)) // Add required scopes
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        return googleSignInClient
    }
}