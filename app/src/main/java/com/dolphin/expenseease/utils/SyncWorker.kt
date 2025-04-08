package com.dolphin.expenseease.utils

/*import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dolphin.expenseease.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext*/

class SyncWorker {/*(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val expenses = db.expenseDao().getAllSync()
                val budgets = db.budgetDao().getAllSync()
                val wallets = db.myWalletDao().getAllSync()
                val reminders = db.reminderDao().getAllSync()

                GoogleSpreadSheetHelper.syncDataToSpreadSheet(applicationContext, expenses, budgets, wallets, reminders)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }*/
}