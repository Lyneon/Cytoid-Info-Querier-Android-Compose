package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import java.lang.Thread.UncaughtExceptionHandler

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        CrashReport.initCrashReport(this, "e3dc58f371", true)
        MMKV.initialize(this)
    }
}

class CrashHandler : UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        e.printStackTrace()
        val intent = Intent()
        intent.setClass(BaseApplication.context, CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(CrashActivity.KEY_EXTRA_CRASH_MESSAGE, e.stackTraceToString())
        BaseApplication.context.startActivity(intent)
        Looper.getMainLooper()
    }
}