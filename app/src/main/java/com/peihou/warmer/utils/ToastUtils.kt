package com.peihou.warmer.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    fun toastShort(context: Context,content:String){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show()
    }
    fun toastShort(context:Context,resId:Int){
        Toast.makeText(context,resId,Toast.LENGTH_SHORT).show()
    }
    fun  toastLong(context: Context,content: String){
        Toast.makeText(context,content,Toast.LENGTH_LONG).show()
    }
    fun  toastLong(context: Context,resId: Int){
        Toast.makeText(context,resId,Toast.LENGTH_LONG).show()
    }

}