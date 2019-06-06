package com.peihou.warmer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity

class Set02Activity : BaseActivity() {
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
       return R.layout.activity_set02
    }

    override fun initView() {

    }
    @OnClick(R.id.img_back)
    fun onClick(view:View){
        when(view.id){
            R.id.img_back->finish()
        }
    }


}
