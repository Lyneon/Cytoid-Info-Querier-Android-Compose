package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Looper
import androidx.compose.material3.DrawerState
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity
import com.microsoft.appcenter.crashes.Crashes
import com.tencent.mmkv.MMKV
import java.lang.Thread.UncaughtExceptionHandler


const val isDebugging = true

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: BaseApplication
        lateinit var globalDrawerState: DrawerState
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        MMKV.initialize(this)
    }
}

class CrashHandler : UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        e.printStackTrace()
        Crashes.trackError(e)
        val intent = Intent()
        intent.setClass(BaseApplication.context, CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(CrashActivity.KEY_EXTRA_CRASH_MESSAGE, e.stackTraceToString())
        BaseApplication.context.startActivity(intent)
        Looper.getMainLooper()
    }
}