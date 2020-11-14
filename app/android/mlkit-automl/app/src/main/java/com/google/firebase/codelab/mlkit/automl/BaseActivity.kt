package com.google.firebase.codelab.mlkit.automl

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** Base activity that requests all needed permission at launch */
abstract class BaseActivity : AppCompatActivity(),
  ActivityCompat.OnRequestPermissionsResultCallback {

  private val requiredPermissions: Array<String> by lazy {
    try {
      this.packageManager.getPackageInfo(
        this.packageName,
        PackageManager.GET_PERMISSIONS
      ).requestedPermissions ?: arrayOf()
    } catch (e: PackageManager.NameNotFoundException) {
      arrayOf<String>()
    }
  }

  private fun allPermissionsGranted() = requiredPermissions.none { !isPermissionGranted(it) }

  private fun requestRuntimePermissions() {
    val allNeededPermissions = requiredPermissions.filter { !isPermissionGranted(it) }

    if (allNeededPermissions.isNotEmpty()) {
      ActivityCompat.requestPermissions(
        this,
        allNeededPermissions.toTypedArray(),
        PERMISSION_REQUESTS
      )
    }
  }

  private fun isPermissionGranted(permission: String): Boolean {
    return when (ContextCompat.checkSelfPermission(this, permission)) {
      PackageManager.PERMISSION_GRANTED -> {
        true
      }
      else -> {
        false
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (!allPermissionsGranted()) {
      requestRuntimePermissions()
    }
  }

  companion object {

    private const val PERMISSION_REQUESTS = 1

  }
}
