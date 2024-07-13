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
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import com.lyneon.cytoidinfoquerier.data.constant.CytoidScoreRange
import com.lyneon.cytoidinfoquerier.data.constant.toIntArray
import com.lyneon.cytoidinfoquerier.data.model.graphql.UserRecord
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileWebapi
import com.lyneon.cytoidinfoquerier.util.DateParser.formatToTimeString
import com.lyneon.cytoidinfoquerier.util.ColumnBitmap
import com.lyneon.cytoidinfoquerier.util.DateParser
import com.lyneon.cytoidinfoquerier.util.RowBitmap
import com.lyneon.cytoidinfoquerier.util.extension.enableAntiAlias
import com.lyneon.cytoidinfoquerier.util.extension.isMaxCytoidGrade
import com.lyneon.cytoidinfoquerier.util.extension.roundBitmap
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision
import com.lyneon.cytoidinfoquerier.util.extension.toBitmap
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.lineHeight
import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.extension.textHeight
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
import kotlin.math.abs
import kotlin.math.roundToInt

object ImageHandler {
    fun getRecordsImage(
        profileWebapi: ProfileWebapi,
        records: List<UserRecord>,
        recordsType: String,
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
            URL(profileWebapi.user.avatar.original).toBitmap()
                .scale(avatarDiameter, avatarDiameter, false).roundBitmap()
        canvas.drawBitmap(avatar, padding.toFloat(), padding.toFloat(), null)
        avatar.recycle()

//      绘制CytoidID
        paint.textSize = 200f
        paint.color = Color.parseColor("#FFF8F8F2")
        canvas.drawText(
            profileWebapi.user.uid,
            (padding + avatarDiameter + padding).toFloat(),
            padding + 200f,
            paint
        )
        paint.textSize = 75f
        canvas.drawText(
            "Lv.${profileWebapi.exp.currentLevel}  Rating ${
                if (keep2DecimalPlaces) profileWebapi.rating.setPrecision(2)
                else profileWebapi.rating
            }",
            (padding + avatarDiameter + padding).toFloat(),
            padding + 200f + paint.fontMetrics.descent + padding + 75f,
            paint
        )
        canvas.drawText(
            "${records.size} $recordsType",
            (padding + avatarDiameter + padding).toFloat(),
            padding + 200f + paint.fontMetrics.descent + padding + 75f + padding + 75f,
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

        recordImages.forEach { if (!it.isRecycled) it.recycle() }

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

        paint.color = Color.parseColor("#80000000")
        paint.style = Paint.Style.FILL

        //绘制曲绘
        canvas.drawBitmap(
            try {
                if (record.chart?.level != null) URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap()
                else BaseApplication.context.getDrawable(R.drawable.sayakacry)!!.toBitmap()
            } catch (e: Exception) {
                BaseApplication.context.getDrawable(R.drawable.sayakacry)!!.toBitmap()
            },
            null,
            Rect(0, 0, 576, 360),
            null
        )
//        绘制半透明灰色遮罩层
        canvas.drawRect(0f, 0f, 576f, 360f, paint)

        record.chart?.let { chart ->
            paint.textSize = 40f
            val difficulty = " ${
                chart.name
                    ?: chart.type.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                        else it.toString()
                    }
            } ${chart.difficulty} "
            val difficultyWidth = paint.measureText(difficulty)
            paint.shader = LinearGradient(
                10f, 20f, 10f + difficultyWidth, 70f, when (chart.type) {
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
            canvas.drawText(difficulty, 10f, 60f, paint)
        }


        paint.textSize = 50f
        record.chart?.level.let { canvas.drawText(it?.title ?: "LevelTitle", 10f, 130f, paint) }

        val score = "${record.score} ${
            if (keep2DecimalPlaces) (record.accuracy * 100).setPrecision(2)
            else record.accuracy * 100
        }%"
        val scoreWidth = paint.measureText(score)
        paint.shader = if (record.score >= 995000) LinearGradient(
            10f,
            190f,
            10f + scoreWidth,
            240f,
            if (record.score == 1000000) CytoidColors.maxColor.toIntArray()
            else CytoidColors.sssColor.toIntArray(),
            null, Shader.TileMode.CLAMP
        ) else null
        canvas.drawText(score, 10f, 190f, paint)
        paint.shader = null
        paint.textSize = 30f
        canvas.drawText(
            "Rating ${
                if (keep2DecimalPlaces) (record.rating).setPrecision(2)
                else record.rating
            }", 10f, 230f, paint
        )

        canvas.drawText("Details: ", 10f, 270f, paint)
        paint.color = Color.parseColor("#60a5fa")
        canvas.drawText(
            record.details.perfect.toString(),
            10f + paint.measureText("Details: "),
            270f,
            paint
        )
        paint.color = Color.parseColor("#facc15")
        canvas.drawText(
            record.details.great.toString(),
            10f + paint.measureText("Details: ${record.details.perfect}/"),
            270f,
            paint
        )
        paint.color = Color.parseColor("#4ade80")
        canvas.drawText(
            record.details.good.toString(),
            10f + paint.measureText("Details: ${record.details.perfect}/${record.details.great}/"),
            270f,
            paint
        )
        paint.color = Color.parseColor("#f87171")
        canvas.drawText(
            record.details.bad.toString(),
            10f + paint.measureText("Details: ${record.details.perfect}/${record.details.great}/${record.details.good}/"),
            270f,
            paint
        )
        paint.color = Color.parseColor("#94a3b8")
        canvas.drawText(
            record.details.miss.toString(),
            10f + paint.measureText("Details: ${record.details.perfect}/${record.details.great}/${record.details.good}/${record.details.bad}/"),
            270f,
            paint
        )
        paint.color = Color.parseColor("#ffffff")
        record.chart?.let {
            canvas.drawText(
                "${record.details.maxCombo} combos ${if (record.score == 1000000) "All Perfect" else if (record.details.maxCombo == record.chart.notesCount) "Full Combo" else ""}",
                10f,
                310f,
                paint
            )
        }
        canvas.drawText("Mods:${record.mods}", 10f, 350f, paint)

        return bitmap
    }
}

object CytoidRecordsImageHandler2 {
    val padding = 50
    val avatarDiameter =
        listOf(160f, 70f, 50f).sumOf { Paint().apply { textSize = it }.lineHeight }.ceil.toInt()
    val recordSpacing = 16
    val recordWidth = 576
    val recordHeight = 360
    val headerHeight = 300

    fun getRecordsImage(
        profileWebapi: ProfileWebapi,
        records: List<UserRecord>,
        recordsType: String,
        columnsCount: Int = 5,
        keep2DecimalPlaces: Boolean = true
    ): Bitmap {
//        行数
        val rowsCount =
            if (records.size % columnsCount == 0) records.size / columnsCount else records.size / columnsCount + 1
//        图片的总宽高
        val width = padding * 2 + recordWidth * columnsCount + recordSpacing * (columnsCount - 1)
        val height =
            padding * 2 + headerHeight + padding + recordHeight * rowsCount + recordSpacing * (rowsCount - 1) + 40
//        初始化位图和绘制对象
        val bitmap = ColumnBitmap(padding = padding).apply {
            setBackgroundColor(
                255,
                (CytoidColors.backgroundColor.red * 255).roundToInt(),
                (CytoidColors.backgroundColor.green * 255).roundToInt(),
                (CytoidColors.backgroundColor.blue * 255).roundToInt()
            )
            addBitmap(getHeaderImage(profileWebapi, keep2DecimalPlaces, records, recordsType))
            addSpace(32)
            addBitmap(getRecordsGridImage(rowsCount, columnsCount, records, keep2DecimalPlaces))
            addSpace(32)
            addText(
                "${Date()} | Generated by ${BaseApplication.context.getString(R.string.app_name)}",
                getDefaultPaint().apply { textSize = 32f }
            )
        }

        return bitmap.getBitmap()
    }

    private fun getHeaderImage(
        profileWebapi: ProfileWebapi,
        keep2DecimalPlaces: Boolean,
        records: List<UserRecord>,
        recordsType: String
    ): Bitmap = RowBitmap(contentSpacing = this@CytoidRecordsImageHandler2.padding).apply {
        addBitmap(
            URL(profileWebapi.user.avatar.original).toBitmap()
                .scale(avatarDiameter, avatarDiameter, false).roundBitmap()
        )
        addBitmap(
            ColumnBitmap().apply {
                addText(profileWebapi.user.uid, getDefaultPaint().apply {
                    textSize = 160f
                })
                addText("Lv.${profileWebapi.exp.currentLevel}  Rating ${
                    profileWebapi.rating.run { if (keep2DecimalPlaces) this.setPrecision(2) else this }
                }", getDefaultPaint().apply {
                    textSize = 70f
                })
                addText("${records.size} $recordsType", getDefaultPaint().apply { textSize = 50f })
            }.getBitmap()
        )
    }.getBitmap()

    private fun getRecordsGridImage(
        rowsCount: Int,
        columnsCount: Int,
        records: List<UserRecord>,
        keep2DecimalPlaces: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            columnsCount * recordWidth + (columnsCount - 1) * recordSpacing,
            rowsCount * recordHeight + (rowsCount - 1) * recordSpacing,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap).apply { enableAntiAlias() }
        val recordImages = getRecordImagesListFromRecordsList(records, keep2DecimalPlaces)

        var currentRecordImageIndex = 0
        canvas.drawBitmap(ColumnBitmap(contentSpacing = recordSpacing).apply {
            for (row in 1..rowsCount) {
                addBitmap(RowBitmap(contentSpacing = 16).apply {
                    for (column in 1..columnsCount) {
                        addBitmap(recordImages[currentRecordImageIndex])
                        currentRecordImageIndex++
                        if (currentRecordImageIndex == recordImages.size) break
                    }
                }.getBitmap())
            }
        }.getBitmap(), 0f, 0f, null)

        return bitmap
    }

    private fun getRecordImagesListFromRecordsList(
        records: List<UserRecord>,
        keep2DecimalPlaces: Boolean
    ): List<Bitmap> {
        val recordImages = ArrayList<Bitmap>(records.size)
        for (i in records.indices) {
//          初始化记录图像列表
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

        return recordImages
    }

    private fun getRecordImage(
        record: UserRecord,
        keep2DecimalPlaces: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(576, 360, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { this.enableAntiAlias() }

        val contentPadding = 16
        val difficultySize = 24f
        val chartTitleSize = 32f
        val chartUIDSize = 16f
        val scoreSize = 36f
        val detailsSize = 24f

        //绘制曲绘
        canvas.drawBitmap(
            try {
                if (record.chart?.level != null) URL(record.chart.level.bundle.backgroundImage.thumbnail).toBitmap()
                else BaseApplication.context.getDrawable(R.drawable.sayakacry)!!.toBitmap()
            } catch (e: Exception) {
                BaseApplication.context.getDrawable(R.drawable.sayakacry)!!.toBitmap()
            },
            null,
            Rect(0, 0, 576, 360),
            null
        )
//        绘制半透明灰色遮罩层
        canvas.drawRect(0f, 0f, 576f, 360f, Paint().apply {
            color = Color.parseColor("#80000000")
            style = Paint.Style.FILL
        })

        canvas.drawBitmap(ColumnBitmap(padding = contentPadding).apply {
            record.chart?.let { chart ->
                val difficulty = " ${
                    chart.name
                        ?: chart.type.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                } ${chart.difficulty} "
                val score = "${record.score} ${
                    (record.accuracy * 100).run { if (keep2DecimalPlaces) this.setPrecision(2) else this }
                }%"
                addBitmap(getDifficultyImage(difficulty, chart.type, difficultySize))
                addText(
                    chart.level?.title ?: "ChartTitle",
                    getDefaultPaint().apply { textSize = chartTitleSize }
                )
                addText(
                    chart.level?.uid ?: "ChartUID",
                    getDefaultPaint().apply { textSize = chartUIDSize })
                addText(score, getDefaultPaint().apply {
                    textSize = scoreSize
                    if (record.score in CytoidScoreRange.sss) shader =
                        if (record.score.isMaxCytoidGrade()) LinearGradient(
                            0f,
                            0f,
                            this.measureText(score),
                            this.textHeight,
                            CytoidColors.maxColor.toIntArray(),
                            null,
                            Shader.TileMode.CLAMP
                        ) else LinearGradient(
                            0f,
                            0f,
                            this.measureText(score),
                            this.textHeight,
                            CytoidColors.sssColor.toIntArray(),
                            null,
                            Shader.TileMode.CLAMP
                        )
                }
                )
                addText(
                    "${record.details.maxCombo}x " +
                            "${
                                when (chart.notesCount) {
                                    record.details.perfect -> "AP"
                                    record.details.maxCombo -> "FC"
                                    else -> ""
                                }
                            } | " +
                            "Rating ${
                                record.rating.run {
                                    if (keep2DecimalPlaces) this.setPrecision(
                                        2
                                    ) else this
                                }
                            }",
                    getDefaultPaint().apply { textSize = detailsSize }
                )
                addBitmap(RowBitmap().apply {
                    addText("Details:", getDefaultPaint().apply { textSize = detailsSize })
                    addText(
                        record.details.perfect.toString() + " ",
                        getDefaultPaint().apply {
                            textSize = detailsSize
                            color = CytoidColors.perfectColor.toArgb()
                        }
                    )
                    addText(
                        record.details.great.toString() + " ",
                        getDefaultPaint().apply {
                            textSize = detailsSize
                            color = CytoidColors.greatColor.toArgb()
                        }
                    )
                    addText(
                        record.details.good.toString() + " ",
                        getDefaultPaint().apply {
                            textSize = detailsSize
                            color = CytoidColors.goodColor.toArgb()
                        }
                    )
                    addText(
                        record.details.bad.toString() + " ",
                        getDefaultPaint().apply {
                            textSize = detailsSize
                            color = CytoidColors.badColor.toArgb()
                        }
                    )
                    addText(
                        record.details.miss.toString() + " ",
                        getDefaultPaint().apply {
                            textSize = detailsSize
                            color = CytoidColors.missColor.toArgb()
                        }
                    )
                }.getBitmap())
                addText("Mods:${record.mods}", getDefaultPaint().apply { textSize = detailsSize })
                addText(
                    DateParser.parseISO8601Date(record.date).formatToTimeString(),
                    getDefaultPaint().apply { textSize = detailsSize }
                )
            }
        }.getBitmap(), 0f, 0f, null)

        return bitmap
    }

    private fun getDifficultyImage(
        difficultyText: String,
        difficultyType: String,
        difficultySize: Float
    ): Bitmap {
        val paint = getDefaultPaint().apply { textSize = difficultySize }
        val difficultyWidth = paint.measureText(difficultyText)
        val difficultyHeight = paint.textHeight
        val bitmap = Bitmap.createBitmap(
            difficultyWidth.ceil.toInt() + 10,
            difficultyHeight.ceil.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap).apply { enableAntiAlias() }

        canvas.drawRoundRect(
            0f,
            0f,
            bitmap.width.toFloat(),
            bitmap.height.toFloat(),
            bitmap.height.toFloat() / 2,
            bitmap.height.toFloat() / 2,
            Paint().apply {
                shader = LinearGradient(
                    0f, 0f, difficultyWidth, difficultyHeight, when (difficultyType) {
                        "easy" -> CytoidColors.easyColor.toIntArray()
                        "extreme" -> CytoidColors.extremeColor.toIntArray()
                        else -> CytoidColors.hardColor.toIntArray()
                    }, null, Shader.TileMode.CLAMP
                )
            }
        )
        canvas.drawText(difficultyText, 5f, abs(paint.ascent()), paint)

        return bitmap
    }

    private fun getDefaultPaint() = TextPaint().apply {
        typeface = ResourcesCompat.getFont(
            BaseApplication.context,
            R.font.mplus_rounded_regular
        )
        isAntiAlias = true
        color = Color.WHITE
    }
}
