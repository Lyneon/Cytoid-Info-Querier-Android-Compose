package com.lyneon.cytoidinfoquerier.tool

import android.widget.Toast
import com.lyneon.cytoidinfoquerier.BaseApplication

operator fun String.times(n: Int) = this.repeat(n)

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(BaseApplication.context,this,duration).show()