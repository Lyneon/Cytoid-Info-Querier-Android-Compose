package com.lyneon.cytoidinfoquerier.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lyneon.cytoidinfoquerier.util.AppSettingsMMKVKeys
import com.lyneon.cytoidinfoquerier.util.MMKVId
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow

class DownloadLevelViewModel : ViewModel() {
    val mmkv = MMKV.mmkvWithID(MMKVId.AppSettings.id)
    var saveUriString =
        MutableStateFlow(
            mmkv.decodeString(
                AppSettingsMMKVKeys.DOWNLOAD_LEVEL_SAVE_URI_STRING.name,
                ""
            )!!
        )
}