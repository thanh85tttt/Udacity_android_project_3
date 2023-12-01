package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.Constants.DOWNLOAD_CHANNEL
import com.udacity.Constants.FAIL_DOWNLOAD
import com.udacity.Constants.GLIDE_URL
import com.udacity.Constants.LOAD_APP_URL
import com.udacity.Constants.RETROFIT_URL
import com.udacity.Constants.SUCCESS_DOWNLOAD
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.sendNotification

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private var downloadID: Long = 0

  private lateinit var notificationManager: NotificationManager
  private lateinit var fileName: String
  private lateinit var url: String
  private lateinit var appDownloadManager: DownloadManager
  private lateinit var customButton: LoadingButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    customButton = binding.contentMain.customButton

    val downloadCompleteFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    registerReceiver(receiver, downloadCompleteFilter)
    appDownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    notificationManager = ContextCompat.getSystemService(
      applicationContext,
      NotificationManager::class.java
    ) as NotificationManager
    createChannel(DOWNLOAD_CHANNEL, "Download")

    customButton.setOnClickListener {
      val radioGroup = binding.contentMain.contentMainRadioGroup
      when (radioGroup.checkedRadioButtonId) {
        R.id.glide_radio_button -> setDownloadValues(GLIDE_URL, R.string.glide_radio_text)
        R.id.load_app_radio_button -> setDownloadValues(LOAD_APP_URL, R.string.load_radio_text)
        R.id.retrofit_radio_button -> setDownloadValues(RETROFIT_URL, R.string.retrofit_radio_text)
        else -> setDownloadValues(null, null)
      }
    }
  }

  private fun showToast(message: CharSequence) {
    customButton.startAllAnimation()
    customButton.stopAllAnimation()
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  private fun setDownloadValues(downloadUrl: String?, fileNameResId: Int?) {
    if (downloadUrl.isNullOrBlank() && fileNameResId == null) {
      url = ""
      showToast(getText(R.string.select_to_download))
      return
    }
    url = downloadUrl!!
    fileName = getString(fileNameResId!!)
    download(fileName)
  }

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
      val query = DownloadManager.Query().apply { id?.let { setFilterById(it) } }

      appDownloadQuery(query)
    }
  }

  private fun appDownloadQuery(query: DownloadManager.Query) {
    appDownloadManager.query(query)?.apply {
      if (moveToFirst()) {
        val titleIndex = getColumnIndex(DownloadManager.COLUMN_TITLE)
        val statusIndex = getColumnIndex(DownloadManager.COLUMN_STATUS)

        if (titleIndex >= 0 && statusIndex >= 0) {
          popUpNotification(getString(titleIndex), getInt(statusIndex))
          customButton.stopAllAnimation()
        }
      }
    }
  }

  private fun popUpNotification(title: String, status: Int) {
    val resultStatus =
      if (DownloadManager.STATUS_FAILED == status) FAIL_DOWNLOAD else SUCCESS_DOWNLOAD

    if (checkNotificationPermission()) {
      notificationManager.sendNotification(
        title,
        resultStatus,
        DOWNLOAD_CHANNEL,
        applicationContext
      )
    } else {
      requestNotificationPermission()
    }
  }

  private fun checkNotificationPermission() =
    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

  private fun requestNotificationPermission() =
    requestPermissions(
      arrayOf(Manifest.permission.POST_NOTIFICATIONS),
      PermissionInfo.PROTECTION_DANGEROUS
    )

  private fun download(fileName: String? = null) {
    if (url.isNullOrBlank() || fileName.isNullOrBlank()) {
      customButton.stopAllAnimation()
      return
    }

    val request = DownloadManager.Request(Uri.parse(url))
      .setTitle(fileName)
      .setDescription(getString(R.string.app_description))
      .setRequiresCharging(false)
      .setAllowedOverMetered(true)
      .setAllowedOverRoaming(true)

    downloadID = appDownloadManager.enqueue(request)
    customButton.startAllAnimation()
  }

  private fun createChannel(channelId: String, channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH
      )
      notificationChannel.enableLights(true)
      notificationChannel.lightColor = Color.GREEN
      notificationChannel.enableVibration(true)
      notificationChannel.description = getString(R.string.channel_download)
      notificationManager.createNotificationChannel(notificationChannel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(receiver)
  }
}
