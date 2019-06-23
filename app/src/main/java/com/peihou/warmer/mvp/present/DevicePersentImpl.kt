package com.peihou.warmer.mvp.present

import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.mvp.model.DeviceModelImpl
import com.peihou.warmer.mvp.model.IDeviceModel
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.service.MQService

class DevicePersentImpl:IDevicePersent,IDeviceModel.OnDeviceListener {


    private var deviceModel:IDeviceModel?=null
    private var userView:IUserView
    constructor(userView:IUserView){
        deviceModel=DeviceModelImpl()
        this.userView=userView
    }
    override fun success(code: Int) {
        userView.success(code)
    }

    override fun fail(code: Int) {
        userView.fail(code)
    }

    override fun getData(mqService: MQService?, deviceMac: String?, code: Int) {
        deviceModel?.getData(mqService,deviceMac,code,this)
    }
    override fun sendBasic(mqService: MQService?, device: Device?, code: Byte) {
        deviceModel?.sendBasic(mqService,device,code,this)
    }

    override fun setDevices(activity: BaseActivity, mqService: MQService?, devices: MutableList<Device>) {
        deviceModel?.setDeivices(activity,mqService,devices,this)
    }
}