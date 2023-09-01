package com.lyneon.cytoidinfoquerier.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import com.lyneon.cytoidinfoquerier.tool.startActivityWithoutActivity
import com.lyneon.cytoidinfoquerier.ui.activity.CrashActivity

open class BaseActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            startActivityWithoutActivity<CrashActivity> {
                putExtra(CrashActivity.KEY_EXTRA_CRASH_MESSAGE,throwable.stackTraceToString())
            }
        }
    }
}