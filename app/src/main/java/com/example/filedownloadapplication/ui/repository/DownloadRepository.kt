package com.example.filedownloadapplication.ui.repository

import android.app.Application
import android.content.Context

class DownloadRepository(mApplication:Application) {

    private val downloadUrl = "https://file-examples.com/storage/fe1734aff46541d35a76822/2017/04/file_example_MP4_1920_18MG.mp4"//"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"

    fun getUrl():String{
        return downloadUrl
    }
}