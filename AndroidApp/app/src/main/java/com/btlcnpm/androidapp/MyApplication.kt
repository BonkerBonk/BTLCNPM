package com.btlcnpm.androidapp

import android.app.Application
import android.webkit.WebView

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
    }
}
