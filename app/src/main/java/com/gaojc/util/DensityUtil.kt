package com.gaojc.util

import android.content.Context

/**
 * dp转px | px转dp
 */
object DensityUtil {
    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    fun dpToPx(context: Context, dpValue: Float): Float {
        // 获取屏幕密度
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale)
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    fun pxToDp(context: Context, pxValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale)
    }
}