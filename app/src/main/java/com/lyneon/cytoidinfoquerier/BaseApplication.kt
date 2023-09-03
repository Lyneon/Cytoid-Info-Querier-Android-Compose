package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.tencent.bugly.crashreport.CrashReport

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        CrashReport.initCrashReport(this, "e3dc58f371", true)
    }
}