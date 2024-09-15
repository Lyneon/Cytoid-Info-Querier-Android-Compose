package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.data.model.shared.Level

class SharedViewModel : ViewModel() {
    var sharedLevelForLevelDetailScreen: Level? = null
}