package com.peihou.warmer.activity

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.present.DevicePersentImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.pojo.TimerTack
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import com.peihou.warmer.utils.Utils
import com.weigan.loopview.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.item_timer.view.*
import kotlinx.android.synthetic.main.pop_select_time.view.*
import java.util.*

class TimerActivity : BaseActivity(),IUserView{


    companion object {
        var running=false
    }
    private var adapter:TimerAdapter?=null
    private var timer:Int?=null
    private var timeCount:Int=0

    var hours= mutableListOf<String>()
    var mins= mutableListOf<String>()
    var temps= mutableListOf<String>()
    var deviceDaoImpl:DeviceDaoImpl?=null
    private var deviceMac:String?=null
    override fun initParms(parms: Bundle?) {
        timer=parms?.getInt("timer")
        deviceMac=parms?.getString("deviceMac")
    }


    override fun bindLayout(): Int {
        return R.layout.activity_timer
    }
    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    private var mWeek=0
    private val weekTv = arrayOfNulls<TextView>(7)
    var refresh=0//1为加载，2为刷新
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
            weekTv[0]=tv_week1
            weekTv[1]=tv_week2
            weekTv[2]=tv_week3
            weekTv[3]=tv_week4
            weekTv[4]=tv_week5
            weekTv[5]=tv_week6
            weekTv[6]=tv_week7
            var calendar=Calendar.getInstance()
            val week2 = calendar.get(Calendar.DAY_OF_WEEK)
            timeCount=4
            mWeek=Utils.getWeek(week2)
            selectWeek(mWeek-1)
            tv_name.text="周计划"
            layout.visibility=View.VISIBLE
        }
        deviceDaoImpl=application.deviceDao
        var decoration= MyDecoration()
        decoration.setDeiverHeight(getDimen(R.dimen.dp_20))
                .setColor(Color.parseColor("#1b1311"))
                .setMargin(0f)
        rv_timer.layoutManager=LinearLayoutManager(this)

        rv_timer.addItemDecoration(decoration)
        adapter=TimerAdapter(this,timeCount)
        rv_timer.adapter=adapter
        swipeRefresh.setHeaderView(MyHeadRefreshView(this))
        swipeRefresh.setFooterView(MyLoadMoreView(this))
        var service=Intent(this,MQService::class.java)
        bind=bindService(service,connection, Context.MODE_PRIVATE)
        var filter=IntentFilter("TimerActivity")
        filter.addAction("offline")
        registerReceiver(receiver,filter)
        swipeRefresh.setRefreshListener(object :BaseRefreshListener{
            override fun loadMore() {
                refresh=1
                if (timer==1){
                    countTime.start()
                    devicePresent.getData(mqService,deviceMac,0x21)

                }else if (timer==2){
                    countTime.start()
                    devicePresent.getData(mqService,deviceMac,0x31)
                }
            }

            override fun refresh() {
                refresh=2
                if (timer==1){
                    countTime.start()
                    devicePresent.getData(mqService,deviceMac,0x21)

                }else if (timer==2){
                    countTime.start()
                    devicePresent.getData(mqService,deviceMac,0x31)
                }
            }

        })
    }

    private fun selectWeek(week:Int){
       for (i in 0 ..6){
           if (i==week){
               weekTv[i]?.background=resources.getDrawable(R.drawable.shape_week,this.theme)
               adapter?.notifyDataSetChanged()
           }else{
               weekTv[i]?.background=resources.getDrawable(R.drawable.shape_week2,this.theme)
           }
       }
    }
    var daySelectPosition=0
    var weekSelectPosition=0
    private var weekDaySelectionPosition=0
    @OnClick(R.id.img_back,R.id.tv_save,R.id.tv_week1,R.id.tv_week2,R.id.tv_week3,R.id.tv_week4,R.id.tv_week5,R.id.tv_week6,R.id.tv_week7)
    fun onClick(view: View){
        when(view.id){
            R.id.img_back->finish()
            R.id.tv_week1->{
                mWeek=0
                selectWeek(mWeek)
            }
            R.id.tv_week2->{
                mWeek=1
                selectWeek(mWeek)
            }
            R.id.tv_week3->{
                mWeek=2
                selectWeek(mWeek)
            }
            R.id.tv_week4->{
                mWeek=3
                selectWeek(mWeek)
            }
            R.id.tv_week5->{
                mWeek=4
                selectWeek(mWeek)
            }
            R.id.tv_week6->{
                mWeek=5
                selectWeek(mWeek)
            }
            R.id.tv_week7->{
                mWeek=6
                selectWeek(mWeek)
            }
            R.id.tv_save->{
                if (save==0){
                    ToastUtils.toastShort(this,"请选择一段定时进行保存!")
                }else{
                    if (timer==1){
                        var success=mqService?.sendDayTimer(deviceMac,dayTimerTack)
                        if (success==true){
                            countTime.start()
                        }else{
                            ToastUtils.toastShort(this,"当前网络不可用")
                        }
                        save=0
                    }else if (timer==2){
                        var  success=mqService?.sendWeekTimer(deviceMac,weekTimerTack)
                        if (success==true){
                            countTime.start()
                        }else{
                            ToastUtils.toastShort(this,"当前网络不可用")
                        }
                        save=0
                    }
                }
            }
        }
    }
    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#1b1311")
    }

    override fun isStatusTextColor(status: Boolean): Boolean {
        return true
    }
    override fun onStart() {
        super.onStart()
        running=true
        if (mqService!=null){
            var device=deviceDaoImpl?.findDevice(deviceMac)
            if (device==null){
                var intent=Intent(this,MainActivity::class.java)
                intent.putExtra("reset",1)
                startActivity(intent)
            }else{
                devicePresent.getData(mqService,deviceMac,0x31)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        running=false
    }
    private var devicePresent=DevicePersentImpl(this)
    override fun fail(code: Int) {
        ToastUtils.toastShort(this,"当前网络不可用")
        Log.i("REfresh","--fail:$code")
        if (refresh==1) {
            swipeRefresh.finishLoadMore()
            refresh=0
        }
        else if (refresh==2){
            swipeRefresh.finishRefresh()
            refresh=0
        }
    }

    override fun success(code: Int) {
        Log.i("REfresh","-->success$code")
        countTime.start()
        if (refresh==1) {
            swipeRefresh.finishLoadMore()
            refresh=0
        }
        else if (refresh==2){
            swipeRefresh.finishRefresh()
            refresh=0
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (bind){
            unbindService(connection)
        }
        unregisterReceiver(receiver)
    }
    var mqService: MQService?=null
    var bind=false

    private var connection=object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder: MQService.LocalBinder = p1 as MQService.LocalBinder
            mqService=binder.service
            if (timer==1){
                dayTimerTack=mqService?.mDayTimerTask()!!
                devicePresent.getData(mqService,deviceMac,0x21)
            }else if (timer==2){
                weekTimerTack=mqService?.mGetWeekTimerTask()!!
                devicePresent.getData(mqService,deviceMac,0x31)
            }
        }
    }
    var weekTimerTack= mutableListOf<MutableList<TimerTack>>()
    var dayTimerTack= mutableListOf<TimerTack>()

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            try {
                var action = intent.action
                if ("offline" == action) {
                    ToastUtils.toastShort(this@TimerActivity,"设备已离线")
                }else {
                    var deviceMac2 = intent.getStringExtra("deviceMac")
                    var reset=intent.getIntExtra("reset",0)
                    if (deviceMac==deviceMac2){
                        if (reset==1){
                            var intent2=Intent(this@TimerActivity,MainActivity::class.java)
                            intent2.putExtra("reset",1)
                            startActivity(intent2)
                        }else{
                            if (timer==1){
                                var dayTimerTack2=intent.getSerializableExtra("dayTimerTack") as MutableList<TimerTack>
                                dayTimerTack.clear()
                                dayTimerTack.addAll(dayTimerTack2)
                                adapter?.notifyDataSetChanged()
                            }else if (timer==2){
                                var weekTimerTack2=intent.getSerializableExtra("weekTimerTack") as MutableList<MutableList<TimerTack>>
                                weekTimerTack.clear()
                                weekTimerTack.addAll(weekTimerTack2)
                                Log.i("TimerTaskActivity","-->${weekTimerTack.size}")
                                adapter?.notifyDataSetChanged()
                            }
                        }

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    internal inner class TimerAdapter(private val context: Context,private var timeCount:Int) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
            val view = View.inflate(context, R.layout.item_timer, null)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, i: Int) {
            holder.tv_count.text="${i+1}"
            if (timeCount==12){
                if (dayTimerTack.size==12) {
                    var timerTack = dayTimerTack[i]
                    holder.bind(timerTack)
                }
            }else if (itemCount==4){
                if (weekTimerTack.size==7){
                    Log.i("TimerAdapter","-->${weekTimerTack[mWeek].size}")
                    if (weekTimerTack[mWeek].size==4) {
                        var timerTack = weekTimerTack[mWeek][i]
                        holder.bind(timerTack)
                    }
                }
            }
            holder.rl_timer.setOnClickListener {
                popTimeAndTemp(i)
            }
        }

        override fun getItemCount(): Int {
            Log.i("timeCount","-->$timeCount")
            return timeCount
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rl_timer:RelativeLayout
        var tv_count:TextView
        var tv_timer:TextView
        var tv_temp:TextView

        init {
            rl_timer=itemView.rl_timer
            tv_count=itemView.tv_count
            tv_timer=itemView.tv_timer
            tv_temp=itemView.tv_temp
        }
        fun bind(timerTack: TimerTack){
            var hour=timerTack.hour
            var min=timerTack.min
            var temp=timerTack.temp
            var hour2="$hour"
            var min2="$min"
            if (hour<10){
                hour2="0$hour"
            }
            if (min<10){
                min2="0$min"
            }
            tv_timer.text="$hour2:$min2"
            tv_temp.text="$temp℃"
        }
    }
    var popupWindow:PopupWindow?=null
    var hour=0
    var min=0
    var temp=0
    var save=0

    fun popTimeAndTemp(pisition:Int){
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

        popupWindow?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }
        var calendar=Calendar.getInstance()
        hour=calendar.get(Calendar.HOUR_OF_DAY)
        min=calendar.get(Calendar.MINUTE)

        var timerHour=view.timerHour
        timerHour.setItemsVisibleCount(5)

        timerHour.setDividerColor(Color.WHITE)
        timerHour.setItems(hours)
        timerHour.setCurrentPosition(hour)
        timerHour.setListener{
            index: Int ->  hour=hours[index].toInt()
            Log.i("timerHour","-->hour:$hour")
        }


        var timerMin=view.timerMin
        timerMin.setDividerColor(Color.WHITE)
        timerMin.setItemsVisibleCount(5)
        timerMin.setItems(mins)
        timerMin.setCurrentPosition(min)
        timerMin.setListener{
            index: Int ->  min=mins[index].toInt()
            Log.i("timerHour","-->min:$min")
        }
        var tempPicker=view.tempPicker
        tempPicker.setDividerColor(Color.WHITE)
        tempPicker.setItemsVisibleCount(5)
        tempPicker.setItems(temps)
        temp=temps[temps.size/2].toInt()
        Log.i("timerHour","-->temp:$temp")
        tempPicker.setListener {
            index: Int ->  temp=temps[index].toInt()
            Log.i("timerHour","-->temp:$temp")
        }
        backgroundAlpha(0.6f)
        img_ensure.setOnClickListener {
            popupWindow?.dismiss()
            try {
                if (timer==1){
                    var flag=0 //为0表示可以添加日定时
                    for (timeTask in dayTimerTack){
                        var minTimer=hour*60+min
                        var minTimer2=timeTask.hour*60+min
                        var temp2=timeTask.temp
                        if (minTimer==minTimer2 && temp==temp2) {
                            ToastUtils.toastShort(this,"该时间段温度已存在")
                            flag=1
                            break
                        }
                    }
                    if (flag==0){
                        var timerTack=dayTimerTack[pisition]
                        timerTack.deviceMac=deviceMac
                        timerTack.hour=hour
                        timerTack.min=min
                        timerTack.temp=temp
                        dayTimerTack[pisition]=timerTack
                        save=1
                        ToastUtils.toastShort(this,"请及时点击保存！")
                    }
                }else if (timer==2){
                    var flag=0 //为0表示可以添加周定时
                    var weekSelectTimerTask=weekTimerTack[mWeek]
                    for (timeTask in weekSelectTimerTask){
                        var minTimer=hour*60+min
                        var minTimer2=timeTask.hour*60+min
                        var temp2=timeTask.temp
                        if (minTimer==minTimer2 && temp==temp2) {
                            ToastUtils.toastShort(this,"该时间段温度已存在")
                            flag=1
                            break
                        }
                    }
                    if (flag==0){
                        var timerTack=weekTimerTack[mWeek][pisition]
                        timerTack.deviceMac=deviceMac
                        timerTack.hour=hour
                        timerTack.min=min
                        timerTack.temp=temp
                        weekTimerTack[mWeek][pisition]=timerTack
                        save=1
                        ToastUtils.toastShort(this,"请及时点击保存！")

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
