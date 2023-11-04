package com.example.filedownloadapplication.ui.broadcastReceiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import timber.log.Timber

class DownloadBroadcastReceiver : BroadcastReceiver() {

    var callback: ((Int) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
               // Toast.makeText(context, "Download Completed.", Toast.LENGTH_LONG).show()
            }
        }

    }

}