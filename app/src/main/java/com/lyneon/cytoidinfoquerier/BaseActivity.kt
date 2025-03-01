package com.lyneon.cytoidinfoquerier

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.ComponentActivity
import com.lyneon.cytoidinfoquerier.util.AppSettings
import java.util.Locale


open class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        val langCode = AppSettings.locale ?: "zh_CN"
        val config = Configuration(newBase!!.resources.configuration)
        config.setLocales(LocaleList(Locale(langCode)))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}