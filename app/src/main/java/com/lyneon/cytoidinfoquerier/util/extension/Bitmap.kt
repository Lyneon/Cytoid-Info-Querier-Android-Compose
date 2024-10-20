package com.lyneon.cytoidinfoquerier.util.extension

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.provider.MediaStore
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV

fun Bitmap.saveIntoMediaStore(
    contentResolver: ContentResolver = BaseApplication.context.contentResolver,
    contentValues: ContentValues = ContentValues(),
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
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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