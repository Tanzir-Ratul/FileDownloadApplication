package com.example.filedownloadapplication.ui.repository

import android.app.Application
import com.example.filedownloadapplication.utils.downloadUrl

class DownloadRepository(mApplication:Application) {

    fun getUrl():String{
        if (downloadUrl.isEmpty())
            throw Exception("Url is empty")
        return downloadUrl.trim()
    }
}