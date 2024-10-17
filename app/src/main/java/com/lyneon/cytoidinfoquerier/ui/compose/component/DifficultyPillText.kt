package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.data.constant.CytoidColors
import java.util.Locale

@Composable
fun DifficultyPillText(
    modifier: Modifier = Modifier,
    difficultyName: String?,
    difficultyLevel: Int,
    difficultyType: String
) {
    Text(
        text = " ${
            difficultyName
                ?: difficultyType.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
        } $difficultyLevel ",
        maxLines = 1,
        color = Color.White,
        modifier = modifier
            .background(
                Brush.linearGradient(
                    when (difficultyType) {
                        "easy" -> CytoidColors.easyColor
                        "extreme" -> CytoidColors.extremeColor
                        else -> CytoidColors.hardColor
                    }
                ), RoundedCornerShape(CornerSize(100))
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}