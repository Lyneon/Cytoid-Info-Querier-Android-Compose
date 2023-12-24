package com.lyneon.cytoidinfoquerier.logic.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.logic.ImageHandler
import com.lyneon.cytoidinfoquerier.logic.NotificationHandler
import com.lyneon.cytoidinfoquerier.logic.NotificationHandler.registerNotificationChannel
import com.lyneon.cytoidinfoquerier.logic.network.NetRequest
import com.lyneon.cytoidinfoquerier.tool.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.tool.extension.showToast
import com.lyneon.cytoidinfoquerier.ui.compose.QueryType
import com.lyneon.cytoidinfoquerier.ui.compose.response
import com.lyneon.cytoidinfoquerier.ui.compose.responseIsInitialized
import kotlin.concurrent.thread

class ImageGenerateService : Service() {

    companion object {
        fun getStartIntent(
            context: Context,
            cytoidID: String,
            columnsCount: Int,
            queryType: String,
            keep2DecimalPlaces: Boolean
        ) = Intent(context, ImageGenerateService::class.java).apply {
            this.putExtra("cytoidID", cytoidID)
            this.putExtra("columnsCount", columnsCount)
            this.putExtra("queryType", queryType)
            this.putExtra("keep2DecimalPlaces", keep2DecimalPlaces)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (responseIsInitialized()) {
            if (intent == null) throw Exception("intent cannot be null")
            val cytoidID =
                intent.getStringExtra("cytoidID") ?: throw Exception("cytoidID extra needed")
            val columnsCount = intent.getIntExtra("columnsCount", 6)
            val queryType =
                intent.getStringExtra("queryType") ?: throw Exception("queryType extra needed")
            val keep2DecimalPlaces =
                intent.getBooleanExtra("keep2DecimalPlaces", true)

            registerNotificationChannel(
                NotificationHandler.CHANNEL_ID_GENERATE_IMAGE,
                NotificationHandler.CHANNEL_NAME_GENERATE_IMAGE
            )
            val notification =
                NotificationCompat.Builder(
                    BaseApplication.context,
                    NotificationHandler.CHANNEL_ID_GENERATE_IMAGE
                )
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("正在生成图像")
                    .setSilent(true)
                    .setOngoing(true)
                    .build()
            startForeground(
                NotificationHandler.NOTIFICATION_ID_GENERATE_IMAGE,
                notification
            )

            BaseApplication.context.resources.getString(
                R.string.saving
            ).showToast()
            thread {
                ImageHandler.getRecordsImage(
                    NetRequest.getProfile(
                        cytoidID
                    ),
                    if (queryType == QueryType.bestRecords) response.data.profile.bestRecords
                    else response.data.profile.recentRecords,
                    columnsCount,
                    keep2DecimalPlaces
                ).saveIntoMediaStore()
                Looper.prepare()
                BaseApplication.context.resources.getString(
                    R.string.saved
                ).showToast()
                this.stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}