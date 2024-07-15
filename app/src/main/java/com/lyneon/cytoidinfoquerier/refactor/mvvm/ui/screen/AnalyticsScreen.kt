package com.lyneon.cytoidinfoquerier.refactor.mvvm.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.model.graphql.BestRecords
import com.lyneon.cytoidinfoquerier.refactor.mvvm.data.repository.BestRecordsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AnalyticsScreen() {
    var ctdID by remember { mutableStateOf("") }
    var b30 by remember { mutableStateOf<BestRecords?>(null) }

    Column {
        TextField(
            value = ctdID,
            onValueChange = { ctdID = it }
        )
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.IO) {
                    b30 = BestRecordsRepository().getBestRecords(ctdID, 30)
                }
            }
        }) {
            Text(text = "B30")
        }
        if (b30 != null) {
            Text(text = b30.toString())
        }
    }
}