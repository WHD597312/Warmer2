package com.peihou.warmer.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.present.IUserPresent
import com.peihou.warmer.mvp.present.UserPresentImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity(),IUserView{

    override fun fail(code: Int) {
        if (code==10000)
            ToastUtils.toastShort(this,"账号或密码错误")
        else
            ToastUtils.toastShort(this,"登录失败")
    }

    override fun success(code: Int) {
        ToastUtils.toastShort(this,"登录成功")
        var userId= userPreferences?.getInt("userId",0)
        var intent=Intent(this,MainActivity::class.java)
        intent.putExtra("userId",userId)
        startActivity(intent)
    }


    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_login
    }

    private var userPresent:IUserPresent?=null
    private var params= mutableMapOf<String,Any?>()
    override fun initView() {
        userPresent=UserPresentImpl(this)
        userPreferences=getSharedPreferences("userInfo",Context.MODE_PRIVATE)

    }


    override fun onStart() {
        super.onStart()
        if (resultData==0){
            if (userPreferences?.contains("userName")==true && userPreferences?.contains("userPassword")==true){
                var userId= userPreferences?.getInt("userId",0)
                et_acount.setText(userPreferences?.getString("userName",""))
                et_pswd.setText(userPreferences?.getString("userPassword",""))
                var intent=Intent(this,MainActivity::class.java)
                intent.putExtra("userId",userId)
                startActivity(intent)
            }else if (userPreferences?.contains("userName")==true){
                et_acount.setText(userPreferences?.getString("userName",""))
                et_pswd.setText("")
            }
        }
    }
    private var userPreferences:SharedPreferences?=null

    @OnClick(R.id.btn_login,R.id.btn_register,R.id.btn_forget)
    fun onClick(view: View){
        when(view.id){
            R.id.btn_login->{
                var acount=et_acount.text.toString()
                var pswd=et_pswd.text.toString()
                if (!isEmpty(acount, pswd)){
                    params["userName"]=acount
                    params["password"]=pswd
                    userPresent?.operate(this,2,params)
                }
            }
            R.id.btn_register->{
                var intent=Intent(this,RegisterActivity::class.java)
                intent.putExtra("code",1)
                startActivityForResult(intent,100)
            }
            R.id.btn_forget->{
                var intent=Intent(this,RegisterActivity::class.java)
                intent.putExtra("code",2)
                startActivity(intent)
            }
        }
    }

    fun isEmpty(acount:String,pswd:String):Boolean{
        if (TextUtils.isEmpty(acount)){
            ToastUtils.toastShort(this,"账号不能为空")
            return true
        }
        if (TextUtils.isEmpty(pswd)){
            ToastUtils.toastShort(this,"密码不能为空")
            return true
        }
        return false
    }
    var resultData=0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==100){
            resultData=1
            var acount:String?=data?.getStringExtra("acount")
            var pswd=data?.getStringExtra("pswd")
            et_acount.setText(acount)
            et_pswd.setText(pswd)
        }
    }
}
