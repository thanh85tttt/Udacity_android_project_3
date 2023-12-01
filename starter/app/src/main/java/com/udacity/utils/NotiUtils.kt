package com.udacity.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.Constants.STATUS
import com.udacity.Constants.TITLE
import com.udacity.DetailActivity
import com.udacity.R

private const val NOTIFY_ID = 0

fun NotificationManager.sendNotification(
  title: String,
  status: String,
  id: String,
  context: Context
) {
  val icon = R.drawable.ic_assistant_black_24dp
  val notificationTitle = context.getString(R.string.notification_title)
  val notificationDescription = context.getString(R.string.notification_description)
  val detailActivityIntent = Intent(context, DetailActivity::class.java)
    .putExtra(TITLE, title)
    .putExtra(STATUS, status)

  val detailActivityPendingIntent = PendingIntent.getActivity(
    context,
    NOTIFY_ID,
    detailActivityIntent,
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
  )

  val builder = NotificationCompat.Builder(context, id).apply {
    setContentTitle(notificationTitle)
    setContentText(notificationDescription)
    setContentIntent(detailActivityPendingIntent)
    setSmallIcon(icon)
    addAction(
      icon,
      notificationTitle,
      detailActivityPendingIntent
    )
    setAutoCancel(true)
  }

  notify(NOTIFY_ID, builder.build())
}
