package com.lyneon.cytoidinfoquerier.util.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter

fun Canvas.enableAntiAlias() {
    this.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
}