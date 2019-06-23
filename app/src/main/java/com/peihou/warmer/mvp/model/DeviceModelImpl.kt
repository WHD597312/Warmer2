package com.peihou.warmer.mvp.model

import android.os.AsyncTask
import android.util.Log
import com.peihou.warmer.activity.MainActivity
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.service.MQService

class DeviceModelImpl : IDeviceModel {


    override fun getData(mqService: MQService?, deviceMac: String?, code: Int, onDeviceListener: IDeviceModel.OnDeviceListener) {
        var success = mqService?.getData(deviceMac, code)
        if (success == true)
            onDeviceListener.success(1)
        else
            onDeviceListener.fail(-1)
    }

    override fun sendBasic(mqService: MQService?, device: Device?, code: Byte, onDeviceListener: IDeviceModel.OnDeviceListener) {
        var success = mqService?.sendBasic(code, device)
        if (success == true) {
            onDeviceListener.success(1)
        } else {
            onDeviceListener.fail(-1)
        }
    }

    var mqService: MQService? = null
    var onDeviceListener: IDeviceModel.OnDeviceListener? = null
    override fun setDeivices(activity: BaseActivity, mqService: MQService?, devices: MutableList<Device>, onDeviceListener: IDeviceModel.OnDeviceListener) {
        this.mqService = mqService
        this.onDeviceListener = onDeviceListener
        LoadAllDevice(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, devices)
    }

    inner class LoadAllDevice(activity: BaseActivity) : BaseWeakAsyncTask<MutableList<Device>, Void, Int, BaseActivity>(activity) {
        override fun doInBackground(target: BaseActivity?, vararg params: MutableList<Device>): Int {

            var devices = params[0]

            for (device in devices) {
                var deviceMac = device.deviceMac
                Log.i("LoadAllDevice", "-->$deviceMac")
                mqService?.subscribeTopic(deviceMac)
                mqService?.getData(deviceMac, 0x11)
            }
           return 100
        }

        override fun onPostExecute(target: BaseActivity?, result: Int?) {
            if (result == 100)
                onDeviceListener?.success(3)
            else
                onDeviceListener?.fail(-3)
        }

    }
}