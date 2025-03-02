package com.lyneon.cytoidinfoquerier.util

import com.tencent.mmkv.MMKV

enum class MMKVId(val id: String) {
    AppSettings("app_settings"),
    LastQueryTimeCache("last_query_time_cache")
}

enum class AppSettingsMMKVKeys {
    ENABLE_SENTRY,
    GRID_COLUMNS_COUNT_PORTRAIT,
    GRID_COLUMNS_COUNT_LANDSCAPE,
    APP_USER_CYTOID_ID,
    PICTURE_COMPRESS_FORMAT,
    PICTURE_COMPRESS_QUALITY,
    DOWNLOAD_LEVEL_SAVE_URI_STRING,
    APP_LOCALE
}

var String.lastQueryAnalyticsTime
    get() = MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id)
        .decodeLong("lastQueryAnalyticsTime_$this", -1)
    set(value) {
        MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id).encode("lastQueryAnalyticsTime_$this", value)
    }
var String.lastQueryProfileTime
    get() = MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id)
        .decodeLong("lastQueryProfileTime_$this", -1)
    set(value) {
        MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id).encode("lastQueryProfileTime_$this", value)
    }

fun String.getLastCacheTime(type: String) =
    MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id).decodeLong("last${type}CacheTime_$this", -1)

fun String.setLastCacheTime(type: String, timeStamp: Long) =
    MMKV.mmkvWithID(MMKVId.LastQueryTimeCache.id).encode("last${type}CacheTime_$this", timeStamp)

fun String.getLastBestRecordsCacheTime() = this.getLastCacheTime("BestRecords")
fun String.setLastBestRecordsCacheTime(timeStamp: Long) =
    this.setLastCacheTime("BestRecords", timeStamp)

fun String.getLastRecentRecordsCacheTime() = this.getLastCacheTime("RecentRecords")
fun String.setLastRecentRecordsCacheTime(timeStamp: Long) =
    this.setLastCacheTime("RecentRecords", timeStamp)

fun String.getLastProfileGraphQLCacheTime() = this.getLastCacheTime("ProfileGraphQL")
fun String.setLastProfileGraphQLCacheTime(timeStamp: Long) =
    this.setLastCacheTime("ProfileGraphQL", timeStamp)

fun String.getLastProfileDetailsCacheTime() = this.getLastCacheTime("ProfileDetails")
fun String.setLastProfileDetailsCacheTime(timeStamp: Long) =
    this.setLastCacheTime("ProfileDetails", timeStamp)

fun String.getLastProfileCommentListCacheTime() = this.getLastCacheTime("ProfileCommentList")
fun String.setLastProfileCommentListCacheTime(timeStamp: Long) =
    this.setLastCacheTime("ProfileCommentList", timeStamp)

fun String.getLastProfileScreenDataModelCacheTime() =
    this.getLastCacheTime("ProfileScreenDataModel")

fun String.setLastProfileScreenDataModelCacheTime(timeStamp: Long) =
    this.setLastCacheTime("ProfileScreenDataModel", timeStamp)