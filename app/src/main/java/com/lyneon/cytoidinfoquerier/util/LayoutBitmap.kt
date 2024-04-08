package com.lyneon.cytoidinfoquerier.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.lyneon.cytoidinfoquerier.util.extension.enableAntiAlias
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.lineHeight
import kotlin.math.abs

/**
 * 按照传入的组件自动进行线性布局绘制的位图
 *
 * 在调用getBitmap()之前，不会创建位图对象进行任何实际的绘制操作
 * */
sealed interface LayoutBitmap {
    val componentsList: MutableList<LayoutBitmapComponent>
    fun getBitmap(): Bitmap

    fun drawComponent(component: LayoutBitmapComponent, x: Float, y: Float, canvas: Canvas) {
        when (component) {
            is TextComponent -> canvas.drawText(
                component.text,
                x,
                y + abs(component.paint.ascent()),
                component.paint
            )

            is ImageComponent -> canvas.drawBitmap(component.bitmap, x, y, null)
            is SpaceComponent -> {}
            is RectComponent -> canvas.drawRect(
                x,
                y,
                x + component.width.toFloat(),
                component.height.toFloat(),
                component.paint
            )

            is RoundRectComponent -> canvas.drawRoundRect(
                x,
                y,
                x + component.width.toFloat(),
                component.height.toFloat(),
                component.rx,
                component.ry,
                component.paint
            )

            is BackgroundColorComponent -> canvas.drawARGB(
                component.a,
                component.r,
                component.g,
                component.b
            )
        }
    }

    fun addText(text: String, paint: Paint) {
        componentsList.add(TextComponent(text, paint))
    }

    fun addText(textComponent: TextComponent) {
        componentsList.add(textComponent)
    }

    fun addBitmap(bitmap: Bitmap) {
        componentsList.add(ImageComponent(bitmap))
    }

    fun addBitmap(imageComponent: ImageComponent) {
        componentsList.add(imageComponent)
    }

    fun addSpace(size: Int) {
        componentsList.add(SpaceComponent(size))
    }

    fun addSpace(spaceComponent: SpaceComponent) {
        componentsList.add(spaceComponent)
    }

    fun addRect(width: Int, height: Int, paint: Paint) {
        componentsList.add(RectComponent(width, height, paint))
    }

    fun addRect(rectComponent: RoundRectComponent) {
        componentsList.add(rectComponent)
    }

    fun addRoundRect(w: Int, h: Int, rx: Float, ry: Float, paint: Paint) {
        componentsList.add(RoundRectComponent(w, h, rx, ry, paint))
    }

    fun addRoundRect(roundRectComponent: RoundRectComponent) {
        componentsList.add(roundRectComponent)
    }

    fun setBackgroundColor(a: Int, r: Int, g: Int, b: Int) {
        componentsList.add(BackgroundColorComponent(a, r, g, b))
    }
}

sealed interface LayoutBitmapComponent {
    val width: Int
    val height: Int
}

class TextComponent(val text: String, val paint: Paint) :
    LayoutBitmapComponent {
    override val width: Int
        get() = paint.measureText(text).ceil.toInt()
    override val height: Int
        get() = paint.lineHeight.ceil.toInt()
}

class ImageComponent(val bitmap: Bitmap) : LayoutBitmapComponent {
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height
}

class SpaceComponent(val size: Int) : LayoutBitmapComponent {
    override val width: Int get() = size
    override val height: Int get() = size
}

class RectComponent(val w: Int, val h: Int, val paint: Paint) :
    LayoutBitmapComponent {
    override val width: Int get() = w
    override val height: Int get() = h
}

class RoundRectComponent(
    val w: Int, val h: Int, val rx: Float, val ry: Float, val paint: Paint
) : LayoutBitmapComponent {
    override val width: Int get() = w
    override val height: Int get() = h
}

class BackgroundColorComponent(val a: Int, val r: Int, val g: Int, val b: Int) :
    LayoutBitmapComponent {
    override val width: Int get() = 0
    override val height: Int get() = 0
}

class RowBitmap(
    val padding: Int? = null,
    val contentSpacing: Int? = null
) : LayoutBitmap {
    override val componentsList: MutableList<LayoutBitmapComponent> = mutableListOf()

    override fun getBitmap(): Bitmap {
        val width = componentsList.sumOf { it.width } + 2 * (padding ?: 0) +
                (contentSpacing?.times((componentsList.size - 1)) ?: 0)
        val height = componentsList.maxOf { it.height } + 2 * (padding ?: 0)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { enableAntiAlias() }
        var x = 0f
        padding?.let { x += it }
        componentsList.forEach {
            drawComponent(it, x, 0f + (padding?.toFloat() ?: 0f), canvas)
            x += it.width
            if (contentSpacing != null) {
                drawComponent(SpaceComponent(contentSpacing), x, 0f, canvas)
                x += contentSpacing
            }
        }
        return bitmap
    }
}

class ColumnBitmap(
    val padding: Int? = null,
    val contentSpacing: Int? = null
) : LayoutBitmap {
    override val componentsList: MutableList<LayoutBitmapComponent> = mutableListOf()

    override fun getBitmap(): Bitmap {
        val width = componentsList.maxOf { it.width } + 2 * (padding ?: 0)
        val height = componentsList.sumOf { it.height } + 2 * (padding ?: 0) +
                (contentSpacing?.times((componentsList.size - 1)) ?: 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { enableAntiAlias() }
        var y = 0f
        padding?.let { y += it }
        componentsList.forEach {
            drawComponent(it, 0f + (padding?.toFloat() ?: 0f), y, canvas)
            y += it.height
            if (contentSpacing != null) {
                drawComponent(SpaceComponent(contentSpacing), 0f, y, canvas)
                y += contentSpacing
            }
        }
        return bitmap
    }
}