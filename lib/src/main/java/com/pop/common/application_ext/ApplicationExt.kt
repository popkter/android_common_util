package com.pop.toolslib.application_ext

import android.app.Application
import androidx.annotation.CallSuper

open class ApplicationExt: Application() {

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        ApplicationModule.init(this)
    }

}