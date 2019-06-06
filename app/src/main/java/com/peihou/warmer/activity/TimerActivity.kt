package com.peihou.warmer.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.OnClick
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.view.MyDecoration
import com.peihou.warmer.custom.view.MyHeadRefreshView
import com.peihou.warmer.custom.view.MyLoadMoreView
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.item_timer.view.*
import kotlinx.android.synthetic.main.pop_select_time.view.*

class TimerActivity : BaseActivity() {
    private var adapter:TimerAdapter?=null
    private var timer:Int?=null
    private var timeCount:Int=0

    var hours= mutableListOf<String>()
    var mins= mutableListOf<String>()
    var temps= mutableListOf<String>()
    override fun initParms(parms: Bundle?) {
        timer=parms?.getInt("timer")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_timer
    }
    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }
    override fun initView() {
        for (i in 0..23){
            hours.add(""+i)
        }
        for (i in 0..59){
            mins.add(""+i)
        }
        for (i in 5 ..42){
            temps.add(""+i)
        }
        if (timer==1){
            timeCount=12
            tv_name.text="日计划"
            layout.visibility=View.GONE
        }else if (timer==2){
            timeCount=4
            tv_name.text="周计划"
            layout.visibility=View.VISIBLE
        }
        var decoration= MyDecoration()
        decoration.setDeiverHeight(getDimen(R.dimen.dp_20))
                .setColor(Color.parseColor("#1b1311"))
                .setMargin(0f)
        rv_timer.layoutManager=LinearLayoutManager(this)

        rv_timer.addItemDecoration(decoration)
        adapter=TimerAdapter(this)
        rv_timer.adapter=adapter
        swipeRefresh.setHeaderView(MyHeadRefreshView(this))
        swipeRefresh.setFooterView(MyLoadMoreView(this))
        swipeRefresh.setRefreshListener(object :BaseRefreshListener{
            override fun loadMore() {

            }

            override fun refresh() {

            }

        })
    }
    @OnClick(R.id.img_back)
    fun onClick(view: View){
        when(view.id){
            R.id.img_back->finish()
        }
    }

    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#1b1311")
    }

    internal inner class TimerAdapter(private val context: Context) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val view = View.inflate(context, R.layout.item_timer, null)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, i: Int) {
            holder.tv_count?.text="${i+1}"
            holder.rl_timer?.setOnClickListener {
                popTimeAndTemp()
            }
        }

        override fun getItemCount(): Int {
            return timeCount
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rl_timer:RelativeLayout?=null
        var tv_count:TextView?=null
        var tv_timer:TextView?=null
        var tv_temp:TextView?=null

        init {
            rl_timer=itemView.rl_timer
            tv_count=itemView.tv_count
            tv_timer=itemView.tv_timer
            tv_temp=itemView.tv_temp
        }
    }
    var popupWindow:PopupWindow?=null
    fun popTimeAndTemp(){
        if (popupWindow!=null && (popupWindow?.isShowing==true))
            return
        var view:View=View.inflate(this,R.layout.pop_select_time,null)
        popupWindow = PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        //点击空白处时，隐藏掉pop窗口
        popupWindow?.isFocusable=true
        popupWindow?.isOutsideTouchable=true
        popupWindow?.update()

        popupWindow?.showAtLocation(tv_name, Gravity.BOTTOM or Gravity.CENTER, 0, 0)
        var img_cancel=view.img_cancel
        var img_ensure=view.img_ensure
        img_cancel.setOnClickListener {
            popupWindow?.dismiss()
        }
        img_ensure.setOnClickListener {
            popupWindow?.dismiss()
        }
        popupWindow?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }

        var timerHour=view.timerHour
        timerHour.setItemsVisibleCount(5)
        timerHour.setDividerColor(Color.WHITE)
        timerHour.setItems(hours)
        var timerMin=view.timerMin
        timerMin.setDividerColor(Color.WHITE)
        timerMin.setItemsVisibleCount(5)
        timerMin.setItems(mins)
        var tempPicker=view.tempPicker
        tempPicker.setDividerColor(Color.WHITE)
        tempPicker.setItemsVisibleCount(5)
        tempPicker.setItems(temps)
//        timerHour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
//        timerMin.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
//        tempPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
//        tempPicker.setNumberPickerDividerColor(tempPicker,Color.parseColor("#f2f4f8"))
//        timerHour.minValue=0
//        timerHour.maxValue=23
//        timerMin.minValue=0
//        timerMin.maxValue=59
//        tempPicker.minValue=0
//        tempPicker.maxValue=23
        backgroundAlpha(0.6f)

    }

}
