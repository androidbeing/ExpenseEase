package com.dolphin.expenseease.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

/**
 * Helper class to manage In-App Updates from Google Play
 * Supports both FLEXIBLE and IMMEDIATE update flows
 */
class AppUpdateHelper(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var updateType = AppUpdateType.FLEXIBLE

    companion object {
        private const val TAG = "AppUpdateHelper"
        private const val DAYS_FOR_FLEXIBLE_UPDATE = 3
        private const val PRIORITY_FOR_IMMEDIATE_UPDATE = 4 // High priority updates
    }

    private val installStateUpdateListener: InstallStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                Log.d(TAG, "Downloading: $bytesDownloaded / $totalBytesToDownload")
                onDownloadProgressUpdate(bytesDownloaded, totalBytesToDownload)
            }
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Download completed. Ready to install.")
                onDownloadComplete()
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully")
                appUpdateManager.unregisterListener(installStateUpdateListener)
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update installation failed")
                onUpdateFailed()
            }
            InstallStatus.CANCELED -> {
                Log.w(TAG, "Update canceled by user")
                onUpdateCanceled()
            }
            else -> {
                Log.d(TAG, "Install status: ${state.installStatus()}")
            }
        }
    }

    /**
     * Check for available updates and start update flow if needed
     */
    fun checkForUpdates(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Log.d(TAG, "Update availability: ${appUpdateInfo.updateAvailability()}")

            when {
                // Update is available and allowed
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                    handleUpdateAvailable(appUpdateInfo, updateLauncher)
                }
                // Update is already downloading
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    resumeUpdate(appUpdateInfo, updateLauncher)
                }
                else -> {
                    Log.d(TAG, "No update available")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error checking for updates", exception)
        }
    }

    private fun handleUpdateAvailable(
        appUpdateInfo: AppUpdateInfo,
        updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        // Determine update type based on update priority and staleness
        updateType = when {
            // High priority update or very old version -> Immediate update
            appUpdateInfo.updatePriority() >= PRIORITY_FOR_IMMEDIATE_UPDATE -> {
                Log.d(TAG, "High priority update detected. Using IMMEDIATE update.")
                AppUpdateType.IMMEDIATE
            }
            // Client version staleness days -> Immediate update
            (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE -> {
                Log.d(TAG, "App is stale. Using IMMEDIATE update.")
                AppUpdateType.IMMEDIATE
            }
            // Default to flexible update
            else -> {
                Log.d(TAG, "Using FLEXIBLE update.")
                AppUpdateType.FLEXIBLE
            }
        }

        // Check if the update type is allowed
        val isUpdateAllowed = when (updateType) {
            AppUpdateType.FLEXIBLE -> appUpdateInfo.isFlexibleUpdateAllowed
            AppUpdateType.IMMEDIATE -> appUpdateInfo.isImmediateUpdateAllowed
            else -> false
        }

        if (isUpdateAllowed) {
            startUpdate(appUpdateInfo, updateLauncher)
        } else {
            Log.w(TAG, "Update type $updateType not allowed for this update")
        }
    }

    private fun startUpdate(
        appUpdateInfo: AppUpdateInfo,
        updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                appUpdateManager.registerListener(installStateUpdateListener)
            }

            val updateOptions = AppUpdateOptions.newBuilder(updateType).build()

            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateLauncher,
                updateOptions
            )

            Log.d(TAG, "Update flow started with type: $updateType")
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Error starting update flow", e)
        }
    }

    /**
     * Resume an update that was already in progress
     */
    private fun resumeUpdate(
        appUpdateInfo: AppUpdateInfo,
        updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        Log.d(TAG, "Resuming update flow")
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            // Update is already downloaded, prompt user to install
            onDownloadComplete()
        } else {
            // Continue the update flow
            startUpdate(appUpdateInfo, updateLauncher)
        }
    }

    /**
     * Check if an update is already in progress when app is resumed
     */
    fun checkUpdateOnResume(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If an immediate update is in progress, resume it
            if (updateType == AppUpdateType.IMMEDIATE &&
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                Log.d(TAG, "Immediate update in progress, resuming...")
                resumeUpdate(appUpdateInfo, updateLauncher)
            }

            // If a flexible update is downloaded, prompt user to install
            if (updateType == AppUpdateType.FLEXIBLE &&
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED
            ) {
                Log.d(TAG, "Flexible update downloaded, prompting install...")
                onDownloadComplete()
            }
        }
    }

    /**
     * Complete the flexible update installation
     * Call this when user clicks "Install" after download completes
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
        Log.d(TAG, "Completing update installation...")
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdateListener)
        }
    }

    // Callback methods - override these in your implementation
    var onDownloadProgressUpdate: (Long, Long) -> Unit = { _, _ -> }
    var onDownloadComplete: () -> Unit = {}
    var onUpdateFailed: () -> Unit = {}
    var onUpdateCanceled: () -> Unit = {}
}

