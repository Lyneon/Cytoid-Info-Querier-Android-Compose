package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.lyneon.cytoidinfoquerier.R
import com.lyneon.cytoidinfoquerier.data.model.webapi.ProfileDetails
import com.lyneon.cytoidinfoquerier.util.extension.setPrecision

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserDetailsHeader(
    profileDetails: ProfileDetails,
    keep2DecimalPlaces: Boolean
) {
    val currentExpProgress =
        (profileDetails.exp.totalExp - profileDetails.exp.currentLevelExp) / (profileDetails.exp.nextLevelExp - profileDetails.exp.currentLevelExp).toFloat()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserAvatar(size = 96.dp, profileDetails = profileDetails)
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                Color(this[0].toColorInt()),
                                Color(this[1].toColorInt())
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
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = profileDetails.exp.totalExp.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = profileDetails.exp.nextLevelExp.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { currentExpProgress },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(currentExpProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.level_up_exp_remaining,
                            profileDetails.exp.nextLevelExp - profileDetails.exp.totalExp
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
