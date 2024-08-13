package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserDetailsHeader(
    profileDetails: ProfileDetails,
    keep2DecimalPlaces: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.heightIn(max = 96.dp)
    ) {
        UserAvatar(profileDetails = profileDetails)
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(text = profileDetails.user.uid, style = MaterialTheme.typography.titleLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Lv. ${profileDetails.exp.currentLevel}",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF9EB3FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                Text(
                    text = "Rating ${
                        profileDetails.rating.run {
                            if (keep2DecimalPlaces) setPrecision(2) else this
                        }
                    }",
                    color = Color.Black,
                    modifier = Modifier
                        .background(Color(0xFF6AF5FF), RoundedCornerShape(100))
                        .padding(horizontal = 6.dp)
                )
                profileDetails.tier?.let {
                    val backgroundColorList =
                        it.colorPalette.background.split(",").run {
                            listOf(
                                Color(android.graphics.Color.parseColor(this[0])),
                                Color(android.graphics.Color.parseColor(this[1]))
                            )
                        }
                    Text(
                        text = it.name,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    backgroundColorList,
                                    Offset.Infinite,
                                    Offset.Zero
                                )
                            )
                            .padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
