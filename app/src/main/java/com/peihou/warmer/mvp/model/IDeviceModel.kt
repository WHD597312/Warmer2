package com.peihou.warmer.mvp.model

import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.service.MQService

interface IDeviceModel {
    interface OnDeviceListener{
        fun success(code: Int)
        fun fail(code: Int)
    }
    fun sendBasic(mqService: MQService?,device: Device?,code: Byte,onDeviceListener: OnDeviceListener)
    fun getData(mqService: MQService?,deviceMac:String?,code: Int,onDeviceListener: OnDeviceListener)
    fun setDeivices(activity: BaseActivity,mqService: MQService?,devices:MutableList<Device>,onDeviceListener: OnDeviceListener)
}