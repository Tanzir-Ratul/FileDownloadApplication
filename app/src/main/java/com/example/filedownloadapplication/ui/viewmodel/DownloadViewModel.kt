package com.example.filedownloadapplication.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Environment
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.filedownloadapplication.ui.repository.DownloadRepository
import java.io.File

class DownloadViewModel(private val repository: DownloadRepository) : ViewModel() {

    private val _isDownloadFile:MutableLiveData<String?> = MutableLiveData<String?>()
    val isDownloadFile:LiveData<String?> = _isDownloadFile
    val progress: MutableLiveData<Int> = MutableLiveData()

    fun getUrl():String{
        return repository.getUrl()
    }
    fun downloadFile(): DownloadManager.Request?{
        try{
            val folder =
                File(Environment.getExternalStorageDirectory().toString() + "Download/CMED_Health")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val fileName = repository.getUrl().split("/").last()
            _isDownloadFile.value = "Downloading $fileName"
            return DownloadManager.Request(android.net.Uri.parse(repository.getUrl()))
                .setTitle(fileName)
                .setDescription("Downloading")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "CMED_Health/$fileName"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        }catch (e:Exception){
            e.printStackTrace()
            _isDownloadFile.value = "Download fail ${e.localizedMessage}"
            return null
        }
    }
}