package com.lyneon.cytoidinfoquerier.util.extension

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV


fun Bitmap.saveIntoMediaStore(
    contentResolver: ContentResolver = BaseApplication.context.contentResolver,
    compressFormat: CompressFormat = CompressFormat.valueOf(
        MMKV.mmkvWithID(MMKVId.AppSettings.id).decodeString(
            AppSettingsMMKVKeys.PICTURE_COMPRESS_FORMAT.name,
            CompressFormat.JPEG.name
        )!!
    ),
    quality: Int = MMKV.mmkvWithID(MMKVId.AppSettings.id)
        .decodeInt(AppSettingsMMKVKeys.PICTURE_COMPRESS_QUALITY.name, 80)
) {
    val insertUri =
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValuesOf(
                MediaStore.Images.ImageColumns.MIME_TYPE to when (compressFormat) {
                    CompressFormat.PNG -> "image/png"
                    CompressFormat.JPEG -> "image/jpeg"
                    else -> "image/webp"
                }
            )
        )
    insertUri?.let {
        contentResolver.openOutputStream(it).use { outputStream ->
            outputStream?.let { it1 -> this.compress(compressFormat, quality, it1) }
        }
    }
}

fun Bitmap.roundBitmap(): Bitmap {
    val roundedBitmap = RoundedBitmapDrawableFactory.create(BaseApplication.context.resources, this)
    roundedBitmap.isCircular = true
    return roundedBitmap.toBitmap()
}

fun Bitmap.getRoundedCornerBitmap(cornerRadius: Float): Bitmap {
    val width = this.width
    val height = this.height

    val output = createBitmap(width, height)
    val canvas = Canvas(output)
    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(
            this@getRoundedCornerBitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
    }

    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

    return output
}