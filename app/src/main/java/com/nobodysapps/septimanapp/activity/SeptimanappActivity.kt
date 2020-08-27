package com.nobodysapps.septimanapp.activity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nobodysapps.septimanapp.R
import com.nobodysapps.septimanapp.application.SeptimanappApplication
import com.nobodysapps.septimanapp.dialog.ChooseLanguageDialogFragment
import com.nobodysapps.septimanapp.localization.LocaleHelper


abstract class SeptimanappActivity: AppCompatActivity() {
    fun getSeptimanappApplication(): SeptimanappApplication = (application as SeptimanappApplication)

    private var initialLocale: String? = null
    private var requestCode = 0
    private val requestPermissionLambdas: MutableMap<Pair<Int, String>, PermissionListener> = HashMap()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialLocale = LocaleHelper.getPersistedLocale(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

//    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
//            // update overrideConfiguration with your locale
//            Log.d("SeptimanappActivity", "initial locale: $initialLocale")
//        }
//        super.applyOverrideConfiguration(overrideConfiguration)
//    }

    // for requesting Permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.size == 1) {
            val permission = permissions[0]
            val requestPermissionPair = Pair(requestCode, permission)
            val lambda = requestPermissionLambdas[requestPermissionPair]
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lambda?.onPermissionGranted(permission)
            } else {
                lambda?.onPermissionDenied(permission)
            }
            requestPermissionLambdas.remove(requestPermissionPair)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // for requesting Permissions
    fun withPermission(permission: String, listener: PermissionListener, explicationText: String? = null) {
        if (ActivityCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission) && explicationText != null) {  // repeatedly denied
                showExplicationText(explicationText, permission, listener)
            } else {
                requestPermission(permission, listener)  //TODO handling after "never ask again" was clicked
            }
        } else {
            listener.onPermissionGranted(permission)
        }
    }

    private fun showExplicationText(
        explicationText: String,
        permission: String,
        listener: PermissionListener
    ) {
        AlertDialog.Builder(this).setMessage(explicationText)
            .setNegativeButton(R.string.cancel) { _, _ ->
                listener.onPermissionDenied(permission)
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                requestPermission(permission, listener)
            }.setCancelable(false)
            .create().show()
    }

    private fun requestPermission(
        permission: String,
        listener: PermissionListener
    ) {
        requestPermissionLambdas[Pair(requestCode, permission)] = listener
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        requestCode++
    }

    override fun onResume() {
        super.onResume()
        Log.d(SeptimanappApplication.TAG, "dens: ${resources.displayMetrics.density}")
        Log.d(SeptimanappApplication.TAG, "height: ${resources.displayMetrics.heightPixels}")
        Log.d(SeptimanappApplication.TAG, "width: ${resources.displayMetrics.widthPixels}")
        if (initialLocale != null && initialLocale != LocaleHelper.getPersistedLocale(this)) {
            recreate()
        }
        val preferences = getSeptimanappApplication().sharedPreferences
        if (!preferences.getBoolean(
                KEY_CHOOSE_LANGUAGE_DIALOG_SHOWN, false
            )
        ) {
            preferences.edit().putBoolean(KEY_CHOOSE_LANGUAGE_DIALOG_SHOWN, true).apply()
            Log.d("SeptimanappActivity", "wants to show chooseLanguage")
            ChooseLanguageDialogFragment().show(supportFragmentManager, "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPermissionLambdas.clear()
    }

    companion object {
        private const val KEY_CHOOSE_LANGUAGE_DIALOG_SHOWN = "language_dialog_shown"
    }
}

interface PermissionListener {
    fun onPermissionGranted(permission: String)
    fun onPermissionDenied(permission: String)
}