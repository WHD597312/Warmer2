package com.peihou.warmer.database.dao.impl

import android.content.Context
import com.peihou.warmer.database.DBManager
import com.peihou.warmer.database.dao.DaoSession
import com.peihou.warmer.database.dao.DeviceDao
import com.peihou.warmer.pojo.Device

class DeviceDaoImpl{
    var deviceDao:DeviceDao?=null
    constructor(context:Context){
        var dbManager:DBManager?= DBManager.getInstance(context)
        var session:DaoSession?=dbManager?.daoSession
        deviceDao=session?.deviceDao
    }
    fun insert(device:Device){
       deviceDao?.insert(device)
    }
    fun update(device: Device?){
        deviceDao?.update(device)
    }
    fun delete(device: Device?){
        deviceDao?.delete(device)
    }
    fun deleteAll(){
        deviceDao?.deleteAll()
    }
    fun findDevice(deviceMac: String?):Device?{
        return deviceDao?.queryBuilder()?.where(DeviceDao.Properties.DeviceMac.eq(deviceMac))?.unique()
    }
    fun findDevice(deviceId: Int):Device?{
        return deviceDao?.queryBuilder()?.where(DeviceDao.Properties.DeviceId.eq(deviceId))?.unique()
    }
    fun findAllDevice():MutableList<Device>?{
        return deviceDao?.loadAll()
    }
}