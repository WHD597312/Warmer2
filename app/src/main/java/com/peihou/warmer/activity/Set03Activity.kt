package com.peihou.warmer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set03.*

class Set03Activity : BaseActivity() {
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
       return R.layout.activity_set03
    }

    override fun initView() {
        seekbar.setValue(10f,60f)
        seekbar.setRange(0f,100f,10f)
    }
    @OnClick(R.id.img_back)
    fun onClick(view:View){
        when(view.id){
            R.id.img_back->finish()
        }
    }


}
