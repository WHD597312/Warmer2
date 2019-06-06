package com.peihou.warmer.activity


import android.os.Bundle
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_set04.*


class Set04Activity : BaseActivity() {
    var position: Int? = null
    override fun initParms(parms: Bundle?) {
        position=parms?.getInt("position")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_set04
    }

    override fun initView() {
        if (position==8){
            tv_name.setText("掉电记忆")
            btn_submit.background=getDrawable(R.drawable.shape_submit2)
        }else if (position==9){
            tv_name.setText("输出设置")
            tv_out.setText("输出设置")
            tv_tip.visibility=View.VISIBLE

        }
    }

    @OnClick(R.id.img_back)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> finish()

        }
    }


}
