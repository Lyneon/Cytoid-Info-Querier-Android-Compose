package com.lyneon.cytoidinfoquerier.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lyneon.cytoidinfoquerier.data.model.graphql.ProfileGraphQL
import com.lyneon.cytoidinfoquerier.util.extension.getImageRequestBuilderForCytoid
import com.patrykandpatrick.vico.compose.component.shape.composeShape
import com.patrykandpatrick.vico.core.component.shape.Shapes

@Composable
fun CollectionCard(collection: ProfileGraphQL.ProfileData.Profile.User.CollectionUserListing) {
    Card {
        Box {
            AsyncImage(
                model = getImageRequestBuilderForCytoid(collection.cover.thumbnail).build(),
                contentDescription = collection.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0x80000000))
                    .padding(6.dp)
            ) {
                Text(
                    text = collection.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = collection.slogan,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${collection.levelCount}个关卡",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        Color(0xFF414558),
                        Shapes.pillShape.composeShape()
                    )
                    .padding(6.dp)
            )
        }
    }
}