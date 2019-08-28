package com.nobodysapps.septimanapp.activity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nobodysapps.septimanapp.application.SeptimanappApplication

abstract class SeptimanappActivity: AppCompatActivity() {
    fun getSeptimanappApplication() : SeptimanappApplication = (application as SeptimanappApplication)

    private var requestCode = 0
    private val requestPermissionLambdas: MutableMap<Pair<Int, String>, PermissionListener> = HashMap()

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

    fun withPermission(permission: String, listener: PermissionListener) {
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

    override fun onDestroy() {
        super.onDestroy()
        requestPermissionLambdas.clear()
    }
}

interface PermissionListener {
    fun onPermissionGranted(permission: String)
    fun onPermissionDenied(permission: String)
    fun onPermissionDeniedBefore(permission: String) {}
}