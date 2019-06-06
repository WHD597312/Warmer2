package com.peihou.warmer.activity

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import me.jessyan.autosize.internal.CustomAdapt

class DeviceActivity : BaseActivity(),CustomAdapt{
    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 667f
    }

    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_device
    }

    override fun initView() {

    }
    @OnClick(R.id.img_back,R.id.tv_menu_timer,R.id.tv_menu_set,R.id.tv_menu_state)
    fun onClick(view :View){
        when(view.id){
            R.id.img_back->finish()
            R.id.tv_menu_timer->
                showTimerPop(1)
            R.id.tv_menu_state->
                startActivity(DeviceStateActivity::class.java)
            R.id.tv_menu_set->
                startActivity(AdvanceSetActivity::class.java)
        }
    }

    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#1b1311")
    }
    var timePopupWindow:PopupWindow?=null

    private fun showTimerPop(code:Int) {
        if (timePopupWindow != null && timePopupWindow?.isShowing == true) {
            return
        }
        var view=View.inflate(this,R.layout.popup_timer,null)

        timePopupWindow= PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        timePopupWindow?.isFocusable=true
        timePopupWindow?.isOutsideTouchable=true
        timePopupWindow?.update()

        timePopupWindow?.animationStyle=R.style.Popupwindow
        timePopupWindow?.showAtLocation(tv_cur, Gravity.BOTTOM, 0, 0)
        backgroundAlpha(0.6f)
        timePopupWindow?.setOnDismissListener(object :PopupWindow.OnDismissListener{
            override fun onDismiss() {
                backgroundAlpha(1.0f)
                timePopupWindow?.dismiss()
            }
        })

       var  rl_timer_day=view.rl_timer_day
       var rl_timer_week=view.rl_timer_week
        var img_timer_day=view.img_timer_day
        var  img_timer_week=view.img_timer_week
        var btn_ensure=view.btn_ensure
        if (code==1){
            btn_ensure.visibility=View.GONE
            img_timer_day.visibility=View.GONE
            img_timer_week.visibility=View.GONE
            rl_timer_day.setOnClickListener {

//                img_timer_day.setImageResource(R.mipmap.img_timer_select)
//                img_timer_week.setImageResource(R.mipmap.img_timer_unselect)
                var intent=Intent(this,TimerActivity::class.java)
                intent.putExtra("timer",1)
                startActivity(intent)
                timePopupWindow?.dismiss()
            }
            rl_timer_week.setOnClickListener {
//                img_timer_day.setImageResource(R.mipmap.img_timer_select)
//                img_timer_week.setImageResource(R.mipmap.img_timer_unselect)
                var intent=Intent(this,TimerActivity::class.java)
                intent.putExtra("timer",2)
                startActivity(intent)
                timePopupWindow?.dismiss()
            }

        }else if (code==0){
            btn_ensure.visibility=View.GONE
        }

    }


}
