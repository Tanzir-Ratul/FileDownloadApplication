package com.example.filedownloadapplication.ui.broadcastReceiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import timber.log.Timber

class DownloadBroadcastReceiver : BroadcastReceiver() {

    var callback: ((Int) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onReceive","progressInBroadcast")

        val action = intent?.action

        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            //TODO: Handle download completion
        } else {
            val downloadManager =
                context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId?.toInt() != -1) {
                val query = DownloadManager.Query()
                downloadId?.let { query.setFilterById(it) }
                val cursor = downloadManager.query(query)

                if (cursor != null && cursor.moveToFirst()) {
                    val downloadedBytesColumnIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalBytesColumnIndex =
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    // Check if the columns exist
                    if (downloadedBytesColumnIndex != -1 && totalBytesColumnIndex != -1) {
                        val downloadedBytes = cursor.getLong(downloadedBytesColumnIndex)
                        val totalBytes = cursor.getLong(totalBytesColumnIndex)
                        val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                        // Update the ViewModel with the progress
                        Log.d("progressInBroadcast","progressInBroadcast $progress")
                        callback?.invoke(progress)
                    }

                }
                cursor.close()
            }
        }
    }

}