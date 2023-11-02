package com.example.filedownloadapplication.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.filedownloadapplication.ui.repository.DownloadRepository

class DownloadViewModel(private val repository: DownloadRepository) : ViewModel() {


    fun getUrl(): String {
        return repository.getUrl()
    }
}