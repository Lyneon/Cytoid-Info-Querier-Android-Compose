package com.lyneon.cytoidinfoquerier.util

import android.content.res.Configuration
import com.lyneon.cytoidinfoquerier.BaseApplication

object OrientationUtils {
    val cardinalOrientations: Int
        get() = BaseApplication.context.resources.configuration.orientation

    val isLandscape: Boolean
        get() = cardinalOrientations == Configuration.ORIENTATION_LANDSCAPE

    val isPortrait: Boolean
        get() = cardinalOrientations == Configuration.ORIENTATION_PORTRAIT
}