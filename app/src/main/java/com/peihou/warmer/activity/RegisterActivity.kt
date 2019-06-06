package com.peihou.warmer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    var code:Int?=null
    var phone:String?=null
    override fun initParms(parms: Bundle?) {
        code=parms?.getInt("code")
        phone=parms?.getString("phone")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_register
    }

    override fun initView() {
        if (code==2){
            tv_login.text="忘记密码"
        }else if (code==3){
            tv_login.text="更改密码"
            et_acount.setText(phone)
            et_acount.isFocusable=false
            et_acount.isFocusableInTouchMode=false
        }
    }

}
