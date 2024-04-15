package com.lyneon.cytoidinfoquerier.util.extension

import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R

/**
 * @param data 传入给ImageRequest.Builder.data()的data参数
 *
 * 接受的默认类型如下：
 * - [String] (mapped to a [Uri])
 * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
 * - [HttpUrl]
 * - [File]
 * - [DrawableRes]
 * - [Drawable]
 * - [Bitmap]
 * - [ByteArray]
 * - [ByteBuffer]
 */
fun getImageRequestBuilderForCytoid(data: Any) =
    ImageRequest.Builder(BaseApplication.context)
        .data(data)
        .setHeader("User-Agent", "CytoidClient/2.1.1")
        .crossfade(true)
        .error(R.drawable.sayakacry)