package com.peihou.warmer.mvp.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.base.MyApplication
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.pojo.UserInfo
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UserModelImpl:IUserModel {
    private var myApplication: MyApplication?=null
    private var devices= mutableListOf<Device>()
    var onUserModelListener:IUserModel.OnUserModelListener?=null
    var code=0
    var versionName:String=""
    /**
     * 调取后端接口
     * @code 1 注册 2登录 3修改用户密码 5获取设备列表 6更改设备名称 7删除设备
     */
    override fun operate(activity: BaseActivity, code:Int,params: MutableMap<String, Any?>, onUserModelListener: IUserModel.OnUserModelListener) {
        this.code=code
        this.onUserModelListener=onUserModelListener
        myApplication=activity.application as MyApplication
        if (code==8){
            var packageManager=activity.application.packageManager
            try {
                val packageInfo = packageManager.getPackageInfo(activity.packageName, 0)
                versionName = packageInfo.versionName
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        OperateAsync(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params)
    }


    inner class OperateAsync(activity: BaseActivity):BaseWeakAsyncTask<MutableMap<String,Any?>,Void,Int,BaseActivity>(activity) {
        override fun doInBackground(activity: BaseActivity?, vararg params: MutableMap<String, Any?>): Int {
            var param=params[0]
            var returnCode=0
            var url=""
            try {
                if (code==1){
                    url=HttpUtils.ipAddress.plus("user/register")
                }else if (code==2){
                    url=HttpUtils.ipAddress.plus("user/login")
                }else if (code==3){
                    url=HttpUtils.ipAddress.plus("user/updatePassword")
                }else if (code==6 || code==7){
                    url=HttpUtils.ipAddress.plus("device/updateDevice")
                }else if (code==8){
                    url=HttpUtils.ipAddress.plus("device/selectVersion")
                }
                if (code==1 || code==2 || code==3 || code==6 ||code==7) {
                    var result = HttpUtils.requestPost(url, param)
                    if (!TextUtils.isEmpty(result)) {
                        Log.i("OperateAsync","-->$result")
                        var jsonObject = JSONObject(result)
                        returnCode = jsonObject.getInt("returnCode")
                        if (code == 2 && returnCode == 200) {
                            login(jsonObject)
                        } else if (code == 3 && returnCode == 200) {
                            updatePassword(param)
                        }else if (returnCode==200 && code==6){
                           updateDevice(6,param)
                        }else if (returnCode==200 && code==7){
                            updateDevice(7,param)
                        }
                    }
                }else if (code==5){
                    var userId:Int=param["userId"] as Int
                    url=HttpUtils.ipAddress.plus("device/getDeviceList?userId=").plus(userId)
                    var result = HttpUtils.requestGet(url, 2)
                    if (!TextUtils.isEmpty(result)) {
                        var jsonObject = JSONObject(result)
                        returnCode = jsonObject.getInt("returnCode")
                        if (returnCode == 200) {
                            var returnData = jsonObject.getJSONArray("returnData")
                            getDevices(returnData)
                        }
                    }
                }else if (code==8){
                    var result:String=HttpUtils.requestGet(HttpUtils.ipAddress.plus("device/selectVersion"),2)
                    if (!TextUtils.isEmpty(result)){
                        var jsonObject=JSONObject(result)
                        var returnCode=jsonObject.getInt("returnCode")

                        if (returnCode==200){
                            var returnData=jsonObject.getString("returnData")
                            if (returnData!=versionName){
                                returnCode=200
                            }else{
                                returnCode=-200
                            }
                            return returnCode
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return returnCode
        }

        override fun onPostExecute(target: BaseActivity?, result: Int?) {
            when(code){
                1->{
                    when(result){
                        200->onUserModelListener?.success(1)
                        10001->onUserModelListener?.fail(10001)
                        else->onUserModelListener?.fail(0)
                    }
                }
                2->{
                    when(result){
                        200->onUserModelListener?.success(1)
                        10000->onUserModelListener?.fail(10000)
                        else->onUserModelListener?.fail(0)
                    }
                }
                3->{
                    when(result){
                        200->onUserModelListener?.success(1)
                        10002->onUserModelListener?.fail(10002)
                        else->onUserModelListener?.fail(0)
                    }
                }
                5->{
                    when(result){
                        200->onUserModelListener?.success(2)
                        else->onUserModelListener?.fail(-2)
                    }
                }
                6->{
                    when(result){
                        200->onUserModelListener?.success(6)
                        else->onUserModelListener?.fail(-6)
                    }
                }
                7->{
                    when(result){
                        200->onUserModelListener?.success(7)
                        else->onUserModelListener?.fail(-7)
                    }
                }
                8->{
                    Log.i("onUserModelListener","-->$result")
                    when(result){
                        200->onUserModelListener?.success(8)
                        else->onUserModelListener?.fail(-8)
                    }
                }
            }
        }
    }

    override fun getDevice(): MutableList<Device> {
        return devices
    }

    /**
     * 登录
     */
    fun login(jsonObject:JSONObject){
        var gson = Gson()
        var returnData = jsonObject.getJSONObject("returnData")
        var userInfo = gson.fromJson<UserInfo>(returnData.toString(), UserInfo::class.java)
        var preferences = myApplication?.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        var editor = preferences?.edit()
        editor?.putInt("userId", userInfo.userId)
        editor?.putString("userName", userInfo.userName)
        editor?.putString("userPassword", userInfo.userPassword)
        editor?.commit()
    }

    /**
     * 修改密码
     */
    fun updatePassword(params: MutableMap<String, Any?>){
        var preferences = myApplication?.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        var editor = preferences?.edit()
        var userName: String = params["userName"] as String
        var userPassword: String = params["password"] as String
        editor?.putString("userName", userName)
        editor?.putString("userPassword", userPassword)
        editor?.commit()
    }

    /**
     * 获取设备列表
     */
    fun getDevices(returnData:JSONArray){
        var len = returnData.length()
        var deviceDao = myApplication?.deviceDao
        devices.clear()
        deviceDao?.deleteAll()
        for (i in 0 until len) {
            var deviceData = returnData.getJSONObject(i)
            var gson = Gson()
            var device = gson.fromJson<Device>(deviceData.toString(), Device::class.java)
            deviceDao?.insert(device)
            devices.add(device)
        }
    }

    /**
     * 更新设备
     * @code 6更新设备名称 7删除设备
     */
    fun updateDevice(code: Int,param: MutableMap<String, Any?>){
        var deviceDao = myApplication?.deviceDao
        var deviceId=param["deviceId"] as Int
        var device=deviceDao?.findDevice(deviceId)
        if (code==6){
            var deviceName=param["deviceName"] as String
            device?.deviceName=deviceName
            deviceDao?.update(device)
        }else if (code==7){
            deviceDao?.delete(device)
        }
    }
}