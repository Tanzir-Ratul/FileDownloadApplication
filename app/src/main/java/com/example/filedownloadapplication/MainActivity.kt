package com.example.filedownloadapplication

import android.Manifest
import android.app.DownloadManager
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.filedownloadapplication.databinding.ActivityMainBinding
import com.example.filedownloadapplication.ui.broadcastReceiver.DownloadBroadcastReceiver
import com.example.filedownloadapplication.ui.repository.DownloadRepository
import com.example.filedownloadapplication.ui.viewmodel.DownloadViewModel
import com.example.filedownloadapplication.ui.viewmodel.DownloadViewModelFactory
import com.example.filedownloadapplication.utils.appSettingOpen
import com.example.filedownloadapplication.utils.isConnected
import com.example.filedownloadapplication.utils.warningPermissionDialog
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var snackBar: Snackbar? = null
    private val requestCodePermission = 1
    private var downloadId: Long = -1
    private var stopUpdate: Boolean = false
    private  var timer: Timer? = null
    // private lateinit var downloadBroadcastReceiver: DownloadBroadcastReceiver

    private val downloadManager: DownloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    private val updateProgressHandler = UpdateProgressHandler(Looper.getMainLooper())

    private val downloadViewModel: DownloadViewModel by lazy {
        ViewModelProvider(
            this,
            DownloadViewModelFactory(DownloadRepository(application))
        )[DownloadViewModel::class.java]
    }

    private val multiplePermissionList =
        if (Build.VERSION.SDK_INT >= 33) {
            arrayListOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // downloadBroadcastReceiver = DownloadBroadcastReceiver() //register broadcast receiver

        initObjects()
        setObservers()
        onClick()
        /* val filter = IntentFilter()
         filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
         filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)

         ContextCompat.registerReceiver(
             this,
             downloadBroadcastReceiver,
             filter,
             ContextCompat.RECEIVER_NOT_EXPORTED
         )*/
        // updateProgressBar()

    }

    private fun updateProgressBar() {
        timer = Timer()
        val downloadObserver = object : TimerTask() {
            override fun run() {
                Log.d("stopUpdate", "$stopUpdate")
                if (!stopUpdate) {
                    Timber.d("isDownloadComplete $timer")
                    queryDownloadProgress()
                } else {
                    timer?.cancel()
                    timer = null
                }
            }
        }
        timer?.schedule(downloadObserver, 0, 1000) // Update every second

    }

    private fun queryDownloadProgress() {
        val query = DownloadManager.Query()
        Log.d("downloadId", "$downloadId")
        query.setFilterById(downloadId)
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
                Timber.d("progressInBroadcast $progress")
                if (progress == 100) {
                    Log.d("stopUpdateInP", "$stopUpdate")
                    stopUpdate = true
                }
                updateProgressHandler.sendMessage(updateProgressHandler.obtainMessage(progress))

            }

        }

        cursor.close()
    }

    private fun initObjects() {

        snackBar = Snackbar.make(
            binding.rootView,
            "No Internet Connection",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Setting") {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }


    }

    private fun setObservers() {
        downloadViewModel.apply {
            isDownloadFile.observe(this@MainActivity) {
                if (!it.isNullOrEmpty()) {
                    Toast.makeText(this@MainActivity, "$it", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

    }


    private fun onClick() {
        binding.downloadBtn.setOnClickListener {
            if (checkMultiplePermission()) {
                doOperation()
            }
        }
        /* downloadBroadcastReceiver.callback = { progress ->
             downloadViewModel.progress.postValue(progress)
         }*/
    }

    private fun isDownloadComplete(): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val columStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = if (columStatus != -1) cursor.getInt(columStatus) else -1
            return status == DownloadManager.STATUS_SUCCESSFUL
        }
        return false
    }

    private fun doOperation() {
        //Toast.makeText(this, "All Permission Granted", Toast.LENGTH_SHORT).show()
        if (isConnected(this)) {
            if (snackBar?.isShown == true) {
                snackBar?.dismiss()
            }

            stopUpdate = false
            updateProgressBar()


            downloadViewModel.downloadFile()?.let { downloadId = downloadManager.enqueue(it) }
            Log.d("downloadIdIN", "$downloadId")

        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            snackBar?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregisterReceiver(downloadBroadcastReceiver)
    }

    // --------------------------below all permission code-------------------------------------------
    private fun checkMultiplePermission(): Boolean {  //read write permission
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                requestCodePermission
            )
            return false
        }
        return true

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestCodePermission) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element != PackageManager.PERMISSION_GRANTED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    //permission granted successfully
                    doOperation()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true

                            }
                        }
                    }
                    if (someDenied) {
                        appSettingOpen(this)
                    } else {
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    checkMultiplePermission()
                                }

                            }
                        }

                    }
                }
            }
        }
    }

    private inner class UpdateProgressHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val progress = msg.what
            Timber.d("progressInHandler $progress")
            binding.progressBar.progress = progress
            binding.progressBarValueTV.text = "$progress%"
        }
    }
}