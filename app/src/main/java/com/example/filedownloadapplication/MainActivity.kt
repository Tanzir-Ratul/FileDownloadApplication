package com.example.filedownloadapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.filedownloadapplication.databinding.ActivityMainBinding
import com.example.filedownloadapplication.ui.repository.DownloadRepository
import com.example.filedownloadapplication.ui.viewmodel.DownloadViewModel
import com.example.filedownloadapplication.ui.viewmodel.DownloadViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val downloadViewModel: DownloadViewModel by lazy {
        ViewModelProvider(
            this,
            DownloadViewModelFactory(DownloadRepository(application))
        )[DownloadViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}