package com.lyneon.cytoidinfoquerier.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.lyneon.cytoidinfoquerier.BaseApplication

object NotificationHandler {
    const val CHANNEL_ID_GENERATE_IMAGE = "Generate image"
    const val CHANNEL_NAME_GENERATE_IMAGE = "生成图片进度"
    const val NOTIFICATION_ID_GENERATE_IMAGE = 1
    val notificationManager =
        BaseApplication.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun registerNotificationChannel(
        channelID: String,
        channelName: String? = null,
        channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        channelDescription: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android O 及以上需要创建通知渠道
            notificationManager.createNotificationChannel(
                channelID,
                channelName,
                channelImportance,
                channelDescription
            )
        }
    }

    fun NotificationManager.createNotificationChannel(
        channelID: String,
        channelName: String? = null,
        channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        channelDescription: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//Android O 及以上需要创建通知渠道
            val channel = NotificationChannel(channelID, channelName, channelImportance).apply {
                this.description = channelDescription
            }
            this.createNotificationChannel(channel)
        }
    }
}