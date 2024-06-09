package com.dolphin.expenseease

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dolphin.expenseease.databinding.ActivityMainBinding
import com.dolphin.expenseease.utils.ApiEndpoint.GOOGLE_AUTH_URL
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val RC_SIGN_IN = 500

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val googleSignInAccount = task.getResult(ApiException::class.java)
                    val email = googleSignInAccount?.email ?: throw Exception("Email not found")

                    // Get the AccountManager
                    val accountManager = AccountManager.get(this@MainActivity)

                    // Find the Account object matching the email
                    val accounts = accountManager.getAccountsByType("com.google")
                    val account = accounts.find { it.name == email }
                        ?: throw Exception("Account not found for email: $email")

                    val credential = GoogleAccountCredential.usingOAuth2(
                        this@MainActivity,
                        listOf("https://www.googleapis.com/auth/spreadsheets")
                    )
                    credential.selectedAccount = account // Now you can assign the Account object

                    val service = Sheets.Builder(
                        NetHttpTransport(), GsonFactory(), credential
                    )
                        .setApplicationName(getString(R.string.app_name))
                        .build()
                    CoroutineScope(Dispatchers.IO).launch {
                        val spreadsheet = Spreadsheet()
                            .setProperties(SpreadsheetProperties().setTitle("ExpenseEaseSheet"))

                        val createdSpreadsheet = service.spreadsheets().create(spreadsheet)
                            .execute()

                        val newSpreadsheetId = createdSpreadsheet.spreadsheetId
                        println("New spreadsheet created with ID: $newSpreadsheetId")

                        val values = listOf(
                            listOf("Item", "Price", "Quantity"),
                            listOf("Product A", 10.99, 5),
                            listOf("Product B", 5.49, 10)
                        )
                        val body = ValueRange().setValues(values)

                        val spreadsheetId = newSpreadsheetId
                        val range = "Sheet1!A1:C3"

                        val result = service.spreadsheets().values()
                            .update(spreadsheetId, range, body)
                            .setValueInputOption("RAW")
                            .execute()

                        println("Cells updated: ${result.updatedCells}")
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                    // Handle Google Sign-In errors
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle other exceptions (e.g., email or account not found)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.i("AAA", "Result Cancelled...")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val signInClient = getGoogleClient()
            startForResult.launch(signInClient.signInIntent)
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()*/
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_expenses,
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