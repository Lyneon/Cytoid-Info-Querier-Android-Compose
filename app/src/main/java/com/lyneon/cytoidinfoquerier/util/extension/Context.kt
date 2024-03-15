package com.lyneon.cytoidinfoquerier.util.extension

import android.content.Context
import android.content.Intent

inline fun <reified T> Context.startActivity(block : Intent.() -> Unit){
    val intent = Intent(this,T::class.java)
    intent.block()
    this.startActivity(intent)
}