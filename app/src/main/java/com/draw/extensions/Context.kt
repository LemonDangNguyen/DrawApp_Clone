package com.draw.extensions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Context.openSettingPermission(action: String) {
    val intent = Intent(action).apply { data = Uri.fromParts("package", packageName, null) }
    startActivity(intent)
}

fun Context.showToast(msg: String, gravity: Int) {
    val toast: Toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    toast.setGravity(gravity, 0, 0)
    toast.show()
}

fun Context.checkPer(str: Array<String>): Boolean {
    var isCheck = true
    for (i in str) {
        if (ContextCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED)
            isCheck = false
    }

    return isCheck
}

fun Context.checkAllPerGrand(): Boolean {
    val storagePer = checkPer(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    val notificationPer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        checkPer(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
    else true
    val overlayPer = Settings.canDrawOverlays(this)
    val cameraPer = checkPer(arrayOf(Manifest.permission.CAMERA))
    val recordPer = checkPer(arrayOf(Manifest.permission.RECORD_AUDIO))

    return storagePer && notificationPer && overlayPer && cameraPer && recordPer
}