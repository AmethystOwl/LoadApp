package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.udacity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var fileName = ""
    private lateinit var factory: MainViewModelFactory
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        factory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(viewModelStore, factory).get(MainViewModel::class.java)
        binding.contentMain.viewModel = viewModel
        binding.lifecycleOwner = this
        createChannel()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            when {
                binding.contentMain.glideRadioButton.isChecked -> {
                    fileName = Constants.GLIDE
                    viewModel.downloadFile(fileName)
                }
                binding.contentMain.retrofitRadioButton.isChecked -> {
                    fileName = Constants.RETROFIT
                    viewModel.downloadFile(fileName)
                }
                binding.contentMain.udacityRadioButton.isChecked -> {
                    fileName = Constants.LOAD_APP
                    viewModel.downloadFile(fileName)

                }
                else -> {
                    Toast.makeText(this, "Please select a repository", Toast.LENGTH_SHORT).show()
                    fileName = ""
                }
            }

        }

        viewModel.downloadStarted.observe(this, Observer {
            binding.contentMain.retrofitRadioButton.isEnabled = !it
            binding.contentMain.glideRadioButton.isEnabled = !it
            binding.contentMain.udacityRadioButton.isEnabled = !it

        })
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.contentMain.customButton.seekToEnd()
            Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id != -1L) {

                val q = DownloadManager.Query()
                q.setFilterById(id!!)
                val c: Cursor = getSystemService(DownloadManager::class.java).query(q)
                var status = 0
                val title: String
                val subTitle: String

                if (c.moveToFirst()) {
                    status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
                } else {
                    title = fileName
                }
                c.close()
                val builder = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                builder.setContentText(title)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    subTitle = "Success"
                } else {
                    subTitle = "Fail"

                }
                builder.build()
                getSystemService(NotificationManager::class.java).sendNotification(
                    title,
                    subTitle,
                    applicationContext
                )

            }
            viewModel.doneDownloading()
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val ID = 345
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download Channel"
            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    fun NotificationManager.sendNotification(
        title: String,
        desc: String,
        applicationContext: Context
    ) {
        val mainIntent = Intent(applicationContext, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ID,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val descIntent = Intent(applicationContext, DetailActivity::class.java)
        descIntent.putExtra(Constants.EXTRA_NAME, title)
        descIntent.putExtra(Constants.EXTRA_STATUS, desc)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            ID,
            descIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val action = NotificationCompat.Action(null, "Check Status", contentPendingIntent)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_cloud_download_241)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(title)
            .setSubText(desc)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(action)

        notify(ID, builder.build())


    }

}


