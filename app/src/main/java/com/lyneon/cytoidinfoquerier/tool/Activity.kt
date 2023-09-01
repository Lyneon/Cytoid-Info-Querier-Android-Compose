package com.lyneon.cytoidinfoquerier.tool

import android.app.Activity
import android.content.Intent
import com.lyneon.cytoidinfoquerier.BaseApplication

inline fun <reified T> Activity.startActivity(block:Intent.() -> Unit){
    val intent = Intent()
    intent.setClass(this,T::class.java)
    intent.block()
    this.startActivity(intent)
}

inline fun <reified T> startActivityWithoutActivity(block:Intent.() -> Unit){
    val intent = Intent()
    intent.setClass(BaseApplication.context,T::class.java)
    intent.block()
    BaseApplication.context.startActivity(intent)
}