package com.lyneon.cytoidinfoquerier.util.extension

import com.tencent.mmkv.MMKV

var String.lastQueryAnalyticsTime
    get() = MMKV.defaultMMKV().decodeLong("lastQueryAnalyticsTime_$this", -1)
    set(value) {
        MMKV.defaultMMKV().encode("lastQueryAnalyticsTime_$this", value)
    }
var String.lastQueryProfileTime
    get() = MMKV.defaultMMKV().decodeLong("lastQueryProfileTime_$this", -1)
    set(value) {
        MMKV.defaultMMKV().encode("lastQueryProfileTime_$this", value)
    }

/*
inline fun <reified T> String.getLastCacheTime(): Long =
    MMKV.defaultMMKV().decodeLong("last${T::class.simpleName}CacheTime_$this", -1)
inline fun <reified T> String.setLastCacheTime(timeStamp: Long) =
    MMKV.defaultMMKV().encode("last${T::class.simpleName}CacheTime_$this", timeStamp)
*/

fun String.getLastCacheTime(type: String) =
    MMKV.defaultMMKV().decodeLong("last${type}CacheTime_$this", -1)

fun String.setLastCacheTime(type: String, timeStamp: Long) =
    MMKV.defaultMMKV().encode("last${type}CacheTime_$this", timeStamp)

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