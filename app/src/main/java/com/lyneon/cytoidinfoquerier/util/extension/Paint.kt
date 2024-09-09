/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
Original package: com.patrykandpatrick.vico.core.extension
View: https://github.com/patrykandpatrick/vico/blob/v1.15.0/vico/core/src/main/java/com/patrykandpatrick/vico/core/extension/PaintExtensions.kt
package com.patrykandpatrick.vico.core.extension
*/
package com.lyneon.cytoidinfoquerier.util.extension

import android.graphics.Paint

private val fm: Paint.FontMetrics = Paint.FontMetrics()
/*
Unused function

internal fun Paint.withOpacity(opacity: Float, action: (Paint) -> Unit) {
    val previousOpacity = this.alpha
    color = color.copyColor(opacity * previousOpacity / MAX_HEX_VALUE)
    action(this)
    this.alpha = previousOpacity
}
*/

/**
 * Returns the height of a single line of text.
 */
public val Paint.lineHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.bottom - fm.top + fm.leading
    }

/**
 * Returns the height of text.
 */
public val Paint.textHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.descent - fm.ascent
    }

/**
 * Returns the width of the provided text.
 */
public fun Paint.measureText(text: CharSequence): Float =
    measureText(text, 0, text.length)