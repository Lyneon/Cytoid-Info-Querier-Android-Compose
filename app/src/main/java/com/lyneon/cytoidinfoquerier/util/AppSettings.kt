package com.lyneon.cytoidinfoquerier.util

import com.tencent.mmkv.MMKV

object AppSettings {
    private val settingsMMKV = MMKV.mmkvWithID(MMKVId.AppSettings.id)

    var cytoidID: String?
        get() = settingsMMKV.decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, null)
        set(value) {
            settingsMMKV.encode(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, value)
        }
}