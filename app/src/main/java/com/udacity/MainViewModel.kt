package com.udacity

import android.app.Application
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private var _downloadStarted = MutableLiveData<Boolean>()
    val downloadStarted: LiveData<Boolean> get() = _downloadStarted
    private val downloadManager =
        app.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
    private val urls = HashMap<String, String>()
    private val appDesc = app.getString(R.string.app_description)

    init {
        _downloadStarted.value = false
        urls[Constants.GLIDE] = Constants.URL_GLIDE
        urls[Constants.RETROFIT] = Constants.URL_RETROFIT
        urls[Constants.LOAD_APP] = Constants.URL_LOAD_APP

    }

    fun downloadFile(fileName: String) {
        if (fileName != "")
            download(urls[fileName]!!, fileName)
    }

    private fun download(url: String, fileName: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription(appDesc)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.rar")

        downloadManager.enqueue(request)
        _downloadStarted.value = true

    }


    fun doneDownloading() {
        _downloadStarted.value = false
    }
}