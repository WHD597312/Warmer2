package com.peihou.warmer.mvp.present

import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.service.MQService

interface IDevicePersent {

    fun sendBasic(mqService: MQService?,device: Device?,code: Byte)
    fun getData(mqService: MQService?,deviceMac:String?,code: Int)
    fun setDevices(activity: BaseActivity,mqService: MQService?,devices:MutableList<Device>)
}