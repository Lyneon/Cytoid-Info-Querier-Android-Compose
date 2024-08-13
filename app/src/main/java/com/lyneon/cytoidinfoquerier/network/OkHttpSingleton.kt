package com.lyneon.cytoidinfoquerier.network

import okhttp3.OkHttpClient

object OkHttpSingleton {
    val instance: OkHttpClient by lazy {
        OkHttpClient()
    }
}