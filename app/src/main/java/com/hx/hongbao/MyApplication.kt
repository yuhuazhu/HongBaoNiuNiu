package com.hx.hongbao

import android.app.Application
import android.content.Context
import android.view.WindowManager


class MyApplication : Application() {

    private val wmParams = WindowManager.LayoutParams()

    fun getwmParams(): WindowManager.LayoutParams = wmParams

    companion object {
        @JvmStatic
        lateinit var context: Context
            private set

        @JvmStatic
        val app: MyApplication = MyApplication()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        GreenDaoManager.instance
    }
}
