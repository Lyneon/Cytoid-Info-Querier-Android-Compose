@file:Suppress("NAME_SHADOWING")

package com.lyneon.cytoidinfoquerier.logic

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.model.graphql.UserRecord
import com.lyneon.cytoidinfoquerier.model.webapi.Profile
import com.lyneon.cytoidinfoquerier.tool.extension.enableAntiAlias
import com.lyneon.cytoidinfoquerier.tool.extension.roundBitmap
import com.lyneon.cytoidinfoquerier.tool.extension.setPrecision
import com.lyneon.cytoidinfoquerier.tool.extension.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

object ImageHandler {
    fun getRecordsImage(
        profile: Profile,
        records: List<UserRecord>,
        columnsCount: Int = 5,
        keep2DecimalPlaces: Boolean = true
    ): Bitmap {
//        总内容的边距
        val padding = 50
//        每个子记录图像之间的间距
        val recordSpacing = 50
//        每个子记录图像的宽高
        val recordWidth = 576
        val recordHeight = 360
//        用户头像的直径
        val avatarDiameter = 500
//        顶部内容区域的高度
        val headerHeight = 500
//        行数
        val rowsCount =
            if (records.size % columnsCount == 0) records.size / columnsCount else records.size / columnsCount + 1
//        图片的总宽高
        val width = padding * 2 + recordWidth * columnsCount + recordSpacing * (columnsCount - 1)
        val height =
            padding * 2 + headerHeight + padding + recordHeight * rowsCount + recordSpacing * (rowsCount - 1) + 40
//        初始化位图和绘制对象
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply {
            this.enableAntiAlias()
        }
        val paint = TextPaint().apply {
            this.typeface =
                ResourcesCompat.getFont(BaseApplication.context, R.font.mplus_rounded_regular)
            this.isAntiAlias = true
        }

//        为整个图片绘制灰色背景
        canvas.drawARGB(255, 39, 41, 53)

//      绘制用户头像
        val avatar =
            URL(profile.user.avatar.original).toBitmap()
                .scale(avatarDiameter, avatarDiameter, false).roundBitmap()
        canvas.drawBitmap(avatar, padding.toFloat(), padding.toFloat(), null)
        avatar.recycle()

//      绘制CytoidID
        paint.textSize = 200f
        paint.color = Color.parseColor("#FFF8F8F2")
        canvas.drawText(
            profile.user.uid,
            (padding + avatarDiameter + padding).toFloat(),
            padding + 200f,
            paint
        )
        paint.textSize = 75f
        canvas.drawText(
            "Lv.${profile.exp.currentLevel}  Rating ${
                if (keep2DecimalPlaces) profile.rating.setPrecision(2)
                else profile.rating
            }",
            (padding + avatarDiameter + padding).toFloat(),
            padding + 200f + paint.fontMetrics.descent + 75f,
            paint
        )

//      获取记录图像

        val recordImages = ArrayList<Bitmap>(records.size)
        for (i in records.indices) {
//            初始化记录图像列表
            recordImages.add(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
        }
        val countDownLatch = CountDownLatch(records.size)
        val dispatcher = Executors.newFixedThreadPool(32).asCoroutineDispatcher()
        val job = Job()
        for (i in records.indices) {
            val record = records[i]
            CoroutineScope(job + dispatcher).launch {
                val recordImage = async {
                    getRecordImage(record, keep2DecimalPlaces)
                }.await()
                synchronized(ImageHandler::class.java) {
                    recordImages[i].recycle()
                    recordImages[i] = recordImage
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
        job.cancel()
        dispatcher.close()

        paint.color = Color.parseColor("#FF5171DE")
        paint.style = Paint.Style.FILL
//            绘制具体记录图像
        var imageIndex = 0
        var startX = padding
        var startY = padding + avatarDiameter + padding
        //循环绘制网格
        val rows = 1..rowsCount
        val columns = 1..columnsCount
        wholeLoop@ for (row in rows) {
            for (column in columns) {
                if (imageIndex > records.size - 1) break@wholeLoop
                canvas.drawBitmap(
                    recordImages[imageIndex],
                    null,
                    Rect(startX, startY, startX + recordWidth, startY + recordHeight),
                    null
                )
                startX += recordWidth + recordSpacing
                imageIndex++
            }
            startX = padding
            startY += recordHeight + recordSpacing
        }

//        绘制生成时间
        paint.textSize = 30f
        paint.color = Color.WHITE
        canvas.drawText(
            "${Date()} | Generated by ${BaseApplication.context.getString(R.string.app_name)}",
            padding.toFloat(),
            (height - padding).toFloat(),
            paint
        )

        recordImages.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }

        recordImages.clear()

        return bitmap
    }

    private fun getRecordImage(record: UserRecord, keep2DecimalPlaces: Boolean): Bitmap {
        val bitmap = Bitmap.createBitmap(576, 360, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply {
            this.enableAntiAlias()
        }
        val paint = TextPaint().apply {
            this.typeface =
                ResourcesCompat.getFont(BaseApplication.context, R.font.mplus_rounded_regular)
            this.isAntiAlias = true
        }

        paint.color = Color.parseColor("#7F000000")
        paint.style = Paint.Style.FILL

        //绘制曲绘
        canvas.drawBitmap(
            try {
                URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap()
            } catch (e: Exception) {
                BaseApplication.context.getDrawable(R.drawable.sayakacry)!!.toBitmap()
            },
            null,
            Rect(0, 0, 576, 360),
            null
        )
//        绘制半透明灰色遮罩层
        canvas.drawRect(0f, 0f, 576f, 360f, paint)

        paint.textSize = 40f
        val difficultyWidth = paint.measureText(" ${
            record.chart.name
                ?: record.chart.type.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
        } ${record.chart.difficulty} ")
        paint.shader = LinearGradient(
            10f, 20f, 10f + difficultyWidth, 70f, when (record.chart.type) {
                "easy" -> intArrayOf(Color.parseColor("#4CA2CD"), Color.parseColor("#67B26F"))

                "hard" -> intArrayOf(Color.parseColor("#B06ABC"), Color.parseColor("#4568DC"))

                "extreme" -> intArrayOf(
                    Color.parseColor("#6F0000"),
                    Color.parseColor("#200122")
                )

                else -> intArrayOf(Color.parseColor("#B06ABC"), Color.parseColor("#4568DC"))
            }, null, Shader.TileMode.CLAMP
        )
        paint.alpha = 255
        canvas.drawRoundRect(RectF(10f, 20f, 10f + difficultyWidth, 70f), 50f, 50f, paint)
        paint.color = Color.WHITE
        paint.shader = null
        canvas.drawText(" ${
            record.chart.name
                ?: record.chart.type.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
        } ${record.chart.difficulty} ", 10f, 60f, paint)

        paint.textSize = 50f
        canvas.drawText(record.chart.level.title, 10f, 130f, paint)
        canvas.drawText(
            "${record.score} ${
                if (keep2DecimalPlaces) (record.accuracy * 100).setPrecision(2)
                else record.accuracy * 100
            }%",
            10f,
            190f,
            paint
        )
        paint.textSize = 30f
        canvas.drawText(
            "Rating ${
                if (keep2DecimalPlaces) (record.rating).setPrecision(2)
                else record.rating
            }", 10f, 230f, paint
        )
        canvas.drawText(
            "Details: ${record.details.perfect}/${record.details.great}/${record.details.good}/${record.details.bad}/${record.details.miss}",
            10f,
            270f,
            paint
        )
        canvas.drawText(
            "${record.details.maxCombo} combos ${if (record.score == 1000000) "AP" else if (record.details.maxCombo == record.chart.notesCount) "FC" else ""}",
            10f,
            310f,
            paint
        )
        canvas.drawText("Mods:${record.mods}", 10f, 350f, paint)

        return bitmap
    }
}
