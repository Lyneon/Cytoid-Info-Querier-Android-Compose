package com.lyneon.cytoidinfoquerier.util.extension

import coil.request.ImageRequest
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.lyneon.cytoidinfoquerier.R

fun getImageRequestBuilderForCytoid(data: Any) =
    ImageRequest.Builder(BaseApplication.context)
        .data(data)
        .setHeader("User-Agent", "CytoidClient/2.1.1")
        .crossfade(true)
        .error(R.drawable.sayakacry)