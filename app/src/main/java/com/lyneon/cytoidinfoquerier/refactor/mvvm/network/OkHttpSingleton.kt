package com.lyneon.cytoidinfoquerier.refactor.mvvm.network

import okhttp3.OkHttpClient

object OkHttpSingleton {
    val instance: OkHttpClient by lazy {
        OkHttpClient()
    }
}