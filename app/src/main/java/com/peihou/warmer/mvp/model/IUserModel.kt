package com.peihou.warmer.mvp.model

import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.pojo.Device

interface IUserModel {
    interface OnUserModelListener{
        fun success(code: Int)
        fun fail(code:Int)
    }

    /**
     * @code 1 注册 2.登录，3.更改密码 4.忘记密码 5.获取设备列表 6.设置设备名称 7.删除设备
     *
     */
    fun operate(activity:BaseActivity,code: Int,params:MutableMap<String,Any?>,onUserModelListener: OnUserModelListener)
    fun getDevice():MutableList<Device>
}