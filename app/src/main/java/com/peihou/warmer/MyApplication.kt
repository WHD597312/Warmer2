package com.peihou.warmer

import android.app.Application
import android.content.Context


class MyApplication :Application(){


    companion object {
        var context:Context?=null

    }
    override fun onCreate() {
        super.onCreate()
        context=this
    }

}