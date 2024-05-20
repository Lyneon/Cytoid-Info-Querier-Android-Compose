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