package com.lyneon.cytoidinfoquerier.util

import android.content.res.Configuration
import com.lyneon.cytoidinfoquerier.BaseApplication
import com.tencent.mmkv.MMKV

object AppSettings {
    private val settingsMMKV = MMKV.mmkvWithID(MMKVId.AppSettings.id)

    var cytoidID: String?
        get() = settingsMMKV.decodeString(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, null)
        set(value) {
            settingsMMKV.encode(AppSettingsMMKVKeys.APP_USER_CYTOID_ID.name, value)
        }

    var locale: String?
        get() = settingsMMKV.decodeString(AppSettingsMMKVKeys.APP_LOCALE.name, "zh")
        set(value) {
            settingsMMKV.encode(AppSettingsMMKVKeys.APP_LOCALE.name, value)
        }

    var portraitGridColumnsCount: Int
        get() = MMKV.mmkvWithID(MMKVId.AppSettings.id)
            .decodeInt(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name, 1)
        set(value) {
            MMKV.mmkvWithID(MMKVId.AppSettings.id)
                .encode(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_PORTRAIT.name, value)
        }

    var landscapeGridColumnsCount: Int
        get() = MMKV.mmkvWithID(MMKVId.AppSettings.id)
            .decodeInt(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, 2)
        set(value) {
            MMKV.mmkvWithID(MMKVId.AppSettings.id)
                .encode(AppSettingsMMKVKeys.GRID_COLUMNS_COUNT_LANDSCAPE.name, value)
        }

    var currentOrientationGridColumnsCount: Int
        get() =
            if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                portraitGridColumnsCount
            else landscapeGridColumnsCount
        set(value) {
            if (BaseApplication.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                portraitGridColumnsCount = value
            else landscapeGridColumnsCount = value
        }
}