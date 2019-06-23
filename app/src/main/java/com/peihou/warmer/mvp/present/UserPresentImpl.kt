package com.peihou.warmer.mvp.present

import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.mvp.model.IUserModel
import com.peihou.warmer.mvp.model.UserModelImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.pojo.Device

class UserPresentImpl:IUserPresent,IUserModel.OnUserModelListener {


    private var userView:IUserView?=null
    private var userModel:IUserModel=UserModelImpl()
    constructor(userView:IUserView){
        this.userView=userView
    }

    override fun success(code: Int) {
        userView?.success(code)
    }
    override fun fail(code: Int) {
        userView?.fail(code)
    }


    override fun operate(activity: BaseActivity, code: Int,params: MutableMap<String, Any?>) {
        userModel.operate(activity,code,params,this)
    }


    override fun getDevices(): MutableList<Device>{
        return userModel.getDevice()
    }
}