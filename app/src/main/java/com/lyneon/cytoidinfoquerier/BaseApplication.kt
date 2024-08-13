package com.lyneon.cytoidinfoquerier

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Looper
import android.os.Process
import com.lyneon.cytoidinfoquerier.data.constant.CytoidConstant
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity
import com.tencent.mmkv.MMKV
import java.lang.Thread.UncaughtExceptionHandler

class BaseApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: BaseApplication
        var cytoidIsInstalled = false

        fun restartApp() {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
            Process.killProcess(Process.myPid())
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate() {
        super.onCreate()
        context = this
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        for (installedPackage in packageManager.getInstalledPackages(0)) {
            if (installedPackage.packageName == CytoidConstant.gamePackageName) {
                cytoidIsInstalled = true
            }
        }
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