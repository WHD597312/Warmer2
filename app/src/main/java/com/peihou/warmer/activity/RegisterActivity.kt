package com.peihou.warmer.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.mvp.present.IUserPresent
import com.peihou.warmer.mvp.present.UserPresentImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity(),IUserView {
    var userPresent:IUserPresent?=null
    var params= mutableMapOf<String,Any?>()
    var flag = 1
    override fun fail(code: Int) {
        if (flag==1){
            when(code){
                10001->ToastUtils.toastShort(this,"用户名已存在")
                else->ToastUtils.toastShort(this,"注册失败")
            }
        }else if (flag==3 || flag==4){
            if (code==10002){
                ToastUtils.toastShort(this,"用户名不存在")
            }else{
                ToastUtils.toastShort(this,"更改失败")
            }
        }
    }

    override fun success(code: Int) {
        if (flag==1){
            ToastUtils.toastShort(this,"注册成功")
            var intent=Intent()
            intent.putExtra("acount",et_acount.text.toString())
            intent.putExtra("pswd",et_repswd.text.toString())
            setResult(100,intent)
            finish()
        }else if (flag==3){
            ToastUtils.toastShort(this,"更改成功")
            setResult(101)
            finish()
        } else if (flag==4){
            ToastUtils.toastShort(this,"更改成功")
            intent.putExtra("acount",et_acount.text.toString())
            intent.putExtra("pswd",et_repswd.text.toString())
            setResult(100,intent)
            finish()
        }
    }

    var code:Int?=null
    var phone:String?=null
    override fun initParms(parms: Bundle?) {
        code=parms?.getInt("code")
        phone=parms?.getString("phone")
    }

    override fun initView() {
        userPresent=UserPresentImpl(this)
        flag=1
        if (code==2){
            flag=4
            tv_login.text="忘记密码"
            btn_login.text="提交"
        }else if (code==3){
            flag=3
            tv_login.text="更改密码"
            btn_login.text="提交"
            et_acount.setText(phone)
            et_acount.isFocusable=false
            et_acount.isFocusableInTouchMode=false
        }
    }
    override fun bindLayout(): Int {
        return R.layout.activity_register
    }

    @OnClick(R.id.btn_login)
    fun onClick(view:View){
        when(view.id){
            R.id.btn_login->{
                var acount=et_acount.text.toString()
                var pswd=et_pswd.text.toString()
                var repswd=et_repswd.text.toString()
                if (!isEmpry(acount,pswd,repswd)){
                    params["userName"]=acount
                    params["password"]=pswd
                    var code=0
                    if (flag==3 || flag==4){
                        code=3
                    }else if (flag==1){
                        code=1
                    }
                    userPresent?.operate(this,code,params)
                }
            }
        }
    }
    private fun isEmpry(acount:String,pswd:String,repswd:String):Boolean{
        if (TextUtils.isEmpty(acount)){
            ToastUtils.toastShort(this,"账号不能为空")
            return true
        }
        if (TextUtils.isEmpty(pswd)){
            ToastUtils.toastShort(this,"密码不能为空")
            return true
        }
        if (TextUtils.isEmpty(repswd)){
            ToastUtils.toastShort(this,"确认密码不能为空")
            return true
        }
        if (pswd!=repswd){
            ToastUtils.toastShort(this,"两次输入的密码不一致")
            return true
        }
        return false
    }

}
