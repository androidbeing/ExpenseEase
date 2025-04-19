package com.dolphin.expenseease.utils.google

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.services.sheets.v4.SheetsScopes

class GoogleSignInHelper(
    private val activity: Activity,
    private val callback: GoogleSignInCallback
) {
    interface GoogleSignInCallback {
        fun onSuccess(account: GoogleSignInAccount)
        fun onFailure(exception: Exception)
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(SheetsScopes.SPREADSHEETS))
            .build()
        GoogleSignIn.getClient(activity, gso)

        /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Add this line
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        GoogleSignIn.getClient(activity, gso)*/
    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Add this new method to get the sign-in intent
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?) {
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            callback.onSuccess(account)
        } catch (e: ApiException) {
            callback.onFailure(e)
        }
    }
}