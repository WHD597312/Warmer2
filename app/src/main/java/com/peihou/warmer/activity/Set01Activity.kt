package com.peihou.warmer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set01.*

class Set01Activity : BaseActivity() {
    var position:Int?=null
    override fun initParms(parms: Bundle?) {
        position=parms?.getInt("position")
    }

    override fun bindLayout(): Int {
       return R.layout.activity_set01
    }

    override fun initView() {
        if (position==2){
            tv_name.setText("温度校正")
            tv_tip.visibility=View.GONE
        }else if (position==3){
            tv_name.setText("内置传感器的设置范围")
            tv_tip.visibility=View.GONE
        }else if (position==4){
            tv_name.setText("外置传感器的设置范围")
            tv_tip.visibility=View.GONE
        }else if (position==5){
            tv_name.setText("外置传感器限温设置点")
            tv_tip.visibility=View.GONE
        }else if (position==6){
            tv_name.setText("输出延时时间")
            tv_tip.visibility=View.GONE
            tv_flag.setText("分钟")
        }
    }
    @OnClick(R.id.img_back)
    fun onClick(view:View){
        when(view.id){
            R.id.img_back->finish()

        }
    }


}
