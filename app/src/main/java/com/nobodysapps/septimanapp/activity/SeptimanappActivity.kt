package com.nobodysapps.septimanapp.activity

import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.nobodysapps.septimanapp.application.SeptimanappApplication
import com.nobodysapps.septimanapp.dialog.ChooseLanguageDialogFragment
import com.nobodysapps.septimanapp.localization.LocaleHelper


abstract class SeptimanappActivity: AppCompatActivity() {
    fun getSeptimanappApplication() : SeptimanappApplication = (application as SeptimanappApplication)

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

    // for requesting Permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.size == 1) {
            val permission = permissions[0]
            val requestPermissionPair = Pair(requestCode, permission)
            val lambda = requestPermissionLambdas.get(requestPermissionPair)
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                listener.onPermissionDeniedBefore(permission)
            }
            requestPermissionLambdas.put(Pair(requestCode, permission), listener)
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            requestCode++
        } else {
            listener.onPermissionGranted(permission)
        }
    }

    private fun showExplicationText(
        explicationText: String,
        permission: String,
        listener: PermissionListener
    ) {
        val explicationSnackbar = Snackbar.make(//TODO use dialog
            findViewById(R.id.content),
            explicationText,
            Snackbar.LENGTH_INDEFINITE
        )
        explicationSnackbar.setAction(R.string.ok) {
            requestPermission(permission, listener)
        }
        explicationSnackbar.setAction(R.string.cancel) {
            listener.onPermissionDenied(permission)
        }
        explicationSnackbar.show()
    }

    private fun requestPermission(
        permission: String,
        listener: PermissionListener
    ) {
        requestPermissionLambdas.put(Pair(requestCode, permission), listener)
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        requestCode++
    }

    override fun onResume() {
        super.onResume()
        if (initialLocale != null && initialLocale != LocaleHelper.getPersistedLocale(this)) {
            recreate()
        }
        val preferences = getSeptimanappApplication().sharedPreferences
        if (!preferences.getBoolean(
                KEY_CHOOSE_LANGUAGE_DIALOG_SHOWN, false)) {
            preferences.edit().putBoolean(KEY_CHOOSE_LANGUAGE_DIALOG_SHOWN, true).apply()
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
    fun onPermissionDeniedBefore(permission: String) {}
}