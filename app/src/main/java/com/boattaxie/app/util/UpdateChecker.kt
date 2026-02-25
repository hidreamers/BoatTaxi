package com.boattaxie.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Google Play In-App Update Manager
 * Handles app updates through Google Play Store
 */
object UpdateChecker {
    private const val TAG = "UpdateChecker"
    const val UPDATE_REQUEST_CODE = 1001
    
    private var appUpdateManager: AppUpdateManager? = null
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState
    
    sealed class UpdateState {
        object Idle : UpdateState()
        object Checking : UpdateState()
        data class UpdateAvailable(val info: AppUpdateInfo) : UpdateState()
        object Downloading : UpdateState()
        data class Downloaded(val progress: Int = 100) : UpdateState()
        object Installing : UpdateState()
        object NoUpdateAvailable : UpdateState()
        data class Error(val message: String) : UpdateState()
    }
    
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                val progress = if (totalBytesToDownload > 0) {
                    ((bytesDownloaded * 100) / totalBytesToDownload).toInt()
                } else 0
                Log.d(TAG, "Downloading update: $progress%")
                _updateState.value = UpdateState.Downloading
            }
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Update downloaded, ready to install")
                _updateState.value = UpdateState.Downloaded()
            }
            InstallStatus.INSTALLING -> {
                Log.d(TAG, "Installing update...")
                _updateState.value = UpdateState.Installing
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed")
                _updateState.value = UpdateState.Idle
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed")
                _updateState.value = UpdateState.Error("Update installation failed")
            }
            InstallStatus.CANCELED -> {
                Log.d(TAG, "Update cancelled")
                _updateState.value = UpdateState.Idle
            }
            else -> {}
        }
    }
    
    /**
     * Initialize the update manager
     */
    fun initialize(context: Context) {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager?.registerListener(installStateUpdatedListener)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        appUpdateManager?.unregisterListener(installStateUpdatedListener)
    }
    
    /**
     * Check for updates from Google Play
     */
    fun checkForUpdate(context: Context, onResult: (Boolean) -> Unit = {}) {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(context)
            appUpdateManager?.registerListener(installStateUpdatedListener)
        }
        
        _updateState.value = UpdateState.Checking
        
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d(TAG, "Update available! Version code: ${appUpdateInfo.availableVersionCode()}")
                    _updateState.value = UpdateState.UpdateAvailable(appUpdateInfo)
                    onResult(true)
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    Log.d(TAG, "No update available")
                    _updateState.value = UpdateState.NoUpdateAvailable
                    onResult(false)
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    Log.d(TAG, "Update already in progress")
                    _updateState.value = UpdateState.Downloading
                    onResult(true)
                }
                else -> {
                    _updateState.value = UpdateState.NoUpdateAvailable
                    onResult(false)
                }
            }
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Failed to check for updates", e)
            _updateState.value = UpdateState.Error("Failed to check for updates: ${e.message}")
            onResult(false)
        }
    }
    
    /**
     * Start immediate update (forces user to update, app restarts after)
     */
    fun startImmediateUpdate(activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isImmediateUpdateAllowed) {
                
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * Start flexible update (downloads in background, user can continue using app)
     */
    fun startFlexibleUpdate(activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isFlexibleUpdateAllowed) {
                
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }
    
    /**
     * Complete the flexible update (call after download is complete)
     * This will restart the app and install the update
     */
    fun completeUpdate() {
        appUpdateManager?.completeUpdate()
    }
    
    /**
     * Check if there's a downloaded update waiting to be installed
     * Call this in onResume to handle updates downloaded while app was in background
     */
    fun checkForPendingUpdate(activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            // If an update was downloaded but not installed (flexible update)
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d(TAG, "Update downloaded but not installed")
                _updateState.value = UpdateState.Downloaded()
            }
            
            // If an immediate update was interrupted, resume it
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(activity)
            }
        }
    }
    
    /**
     * Schedule daily update check - now just checks via Play Store
     * The actual scheduling is handled by the OS for Play Store updates
     */
    fun scheduleDailyUpdateCheck(context: Context) {
        // Google Play handles automatic update checks
        // We can still initialize for manual checks
        initialize(context)
        Log.d(TAG, "Update checker initialized - Google Play handles automatic updates")
    }
    
    /**
     * Check for update immediately (manual check)
     */
    fun checkForUpdateNow(context: Context) {
        checkForUpdate(context)
    }
    
    /**
     * Legacy compatibility - now returns false as we use Play Store
     */
    suspend fun isUpdateAvailable(context: Context): Boolean {
        // For compatibility with existing code
        // Actual check is done via checkForUpdate
        return false
    }
    
    /**
     * Legacy compatibility - now does nothing as we use Play Store
     */
    suspend fun downloadAndInstallUpdate(context: Context, showNotification: Boolean = true) {
        // Updates are now handled through Google Play
        Log.d(TAG, "Updates are handled through Google Play Store")
    }
}
