package com.example.filedownloadapplication.utils

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

var downloadUrl = "https://file-examples.com/storage/fe1734aff46541d35a76822/2017/04/file_example_MP4_1920_18MG.mp4"

fun appSettingOpen(mContext: Context) {

    Toast.makeText(mContext, "Go to Setting and Enable all Permissions", Toast.LENGTH_SHORT).show()
    val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    settingIntent.data = Uri.parse("package:${mContext.packageName}")
    mContext.startActivity(settingIntent)
}

fun warningPermissionDialog(mContext: Context, listener: DialogInterface.OnClickListener) {
    MaterialAlertDialogBuilder(mContext)
        .setMessage("All Permission are required")
        .setCancelable(false)
        .setPositiveButton("OK", listener)
        .create()
        .show()
}

fun isConnected(context: Context):Boolean{
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return run {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val cap = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        when {
            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}