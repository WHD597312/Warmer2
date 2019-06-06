package com.peihou.warmer.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View

import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils


class LoginActivity : BaseActivity(),IUserView {
    override fun fail(code: Int) {
        when(code){
            1-> ToastUtils.toastShort(this,"手机号码为空")
            2-> ToastUtils.toastShort(this,"不合法的手机号")
            3-> ToastUtils.toastShort(this,"密码为空")
        }

    }

    override fun success() {

    }

    private var deviceDaoImpl:DeviceDaoImpl?=null
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
        MQService.startAction(this)

    }


    private var userPreferences:SharedPreferences?=null






    @OnClick(R.id.btn_login,R.id.btn_register,R.id.btn_forget)
    fun onClick(view: View){
        when(view.id){
            R.id.btn_login->{
                startActivity(MainActivity::class.java)
            }
            R.id.btn_register->{
                var intent=Intent(this,RegisterActivity::class.java)
                intent.putExtra("code",1)
               startActivity(intent)
            }
            R.id.btn_forget->{
                var intent=Intent(this,RegisterActivity::class.java)
                intent.putExtra("code",2)
                startActivity(intent)
            }
        }
    }
}
