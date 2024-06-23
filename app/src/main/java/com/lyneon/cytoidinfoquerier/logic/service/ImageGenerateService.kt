package com.lyneon.cytoidinfoquerier.logic.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.model.graphql.Analytics
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileWebapi
import com.lyneon.cytoidinfoquerier.json
import com.lyneon.cytoidinfoquerier.logic.CytoidRecordsImageHandler2
import com.lyneon.cytoidinfoquerier.logic.NotificationHandler
import com.lyneon.cytoidinfoquerier.logic.NotificationHandler.registerNotificationChannel
import com.lyneon.cytoidinfoquerier.ui.viewmodel.QueryType
import com.lyneon.cytoidinfoquerier.util.extension.saveIntoMediaStore
import com.lyneon.cytoidinfoquerier.util.extension.showToast
import kotlinx.serialization.encodeToString
import kotlin.concurrent.thread

class ImageGenerateService : Service() {

    companion object {
        fun getStartIntent(
            context: Context,
            analytics: Analytics,
            cytoidID: String,
            columnsCount: Int,
            queryType: String,
            queryCount: Int,
            keep2DecimalPlaces: Boolean
        ) = Intent(context, ImageGenerateService::class.java).apply {
            this.putExtra("cytoidID", cytoidID)
            this.putExtra("analytics", json.encodeToString(analytics))
            this.putExtra("columnsCount", columnsCount)
            this.putExtra("queryType", queryType)
            this.putExtra("queryCount", queryCount)
            this.putExtra("keep2DecimalPlaces", keep2DecimalPlaces)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        //TODO("Not yet implemented")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) throw NullPointerException("intent cannot be null")
        val cytoidID =
            intent.getStringExtra("cytoidID")
                ?: throw IllegalArgumentException("cytoidID extra needed")
        val analytics = json.decodeFromString<Analytics>(
            intent.getStringExtra("analytics")
                ?: throw IllegalArgumentException("analytics extra needed")
        )
        val columnsCount = intent.getIntExtra("columnsCount", 6)
        val queryType =
            intent.getStringExtra("queryType") ?: throw Exception("queryType extra needed")
        val keep2DecimalPlaces =
            intent.getBooleanExtra("keep2DecimalPlaces", true)
        val queryCount = intent.getIntExtra("queryCount", 0)

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
            CytoidRecordsImageHandler2.getRecordsImage(
                ProfileWebapi.get(cytoidID),
                if (queryType == QueryType.BestRecords.name) analytics.data.profile!!.bestRecords.subList(
                    0,
                    queryCount
                )
                else analytics.data.profile!!.recentRecords.subList(0, queryCount),
                queryType,
                columnsCount,
                keep2DecimalPlaces
            ).saveIntoMediaStore()
            Looper.prepare()
            BaseApplication.context.resources.getString(
                R.string.saved
            ).showToast()
            this.stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}