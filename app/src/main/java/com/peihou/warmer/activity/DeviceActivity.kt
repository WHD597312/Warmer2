package com.peihou.warmer.activity

import android.content.*
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.dialog.ChangeDialog
import com.peihou.warmer.custom.view.WarmerProgressBar
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.present.DevicePersentImpl
import com.peihou.warmer.mvp.present.IDevicePersent
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.TenTwoUtil
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_device.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import me.jessyan.autosize.internal.CustomAdapt

class DeviceActivity : BaseActivity(), CustomAdapt, IUserView {

    override fun success(code: Int) {
        countTime.start()
    }

    override fun fail(code: Int) {
        if (code == 1)
            ToastUtils.toastShort(this, "当前网络不可用")
    }


    var TAG = "DeviceActivity"
    var device: Device?=null

    var deviceMac: String? = null
    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 667f
    }

    override fun initParms(parms: Bundle?) {
        device=parms?.getSerializable("device") as Device
    }

    override fun bindLayout(): Int {
        return R.layout.activity_device
    }
    companion object {
        var running=false
    }

    var devicePresent: IDevicePersent? = null
    var deviceDao:DeviceDaoImpl?=null
    private var animationDrawable: AnimationDrawable? = null
    override fun initView() {
        back=1
        devicePresent = DevicePersentImpl(this)
        deviceMac=device?.deviceMac

        deviceDao=application.deviceDao
        var service = Intent(this, MQService::class.java)
        bind = bindService(service, connection, Context.BIND_AUTO_CREATE)

        var filter = IntentFilter("DeviceActivity")
        filter.addAction("offline")
        registerReceiver(receiver, filter)
        img_circle.setImageResource(R.drawable.lottery_animlist)
        animationDrawable = img_circle.drawable as AnimationDrawable
        setMode(device!!)
////        img_circle.setImageResource(R.drawable.lottery_animlist)
//        val animationDrawable2 = img_circle.drawable as AnimationDrawable
//        animationDrawable2.start()
        progressBar.setOnMoveListener(object : WarmerProgressBar.OnMoveListener {
            override fun setOnMoveListener(value: Int) {
                Log.i(TAG, "-->$value")
                device?.setTemp = value
                devicePresent?.sendBasic(mqService, device, 0x11)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        running=true

        device=deviceDao?.findDevice(deviceMac)
        if (device==null){
            setResult(1000)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        running=false
    }
    override fun onDestroy() {
        super.onDestroy()
        if (bind) {
            unbindService(connection)
        }

        unregisterReceiver(receiver)

    }

    override fun isStatusTextColor(status: Boolean): Boolean {
        return false
    }

    @OnClick(R.id.img_back, R.id.image_switch, R.id.img_hand, R.id.img_lock, R.id.img_ai, R.id.img_timer,R.id.tv_menu_timer, R.id.tv_menu_set, R.id.tv_menu_restore,R.id.tv_menu_state)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> finish()
            R.id.image_switch -> {
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline() == true) {
                    var state = device?.state
                    var open = device?.open
                    var x = TenTwoUtil.changeToTwo(state!!)
                    if (open == 1) {
                        x[0] = 1
                        x[1] = 0
                    } else if (open == 0) {
                        x[0] = 0
                        x[1] = 1
                    }
                    state = TenTwoUtil.changeToTen2(x)
                    device?.state = state
                    devicePresent?.sendBasic(mqService, device, 0x11)
                }
            }
            R.id.img_hand -> {
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline() == true) {
                    device?.mode = 0x01
                    devicePresent?.sendBasic(mqService, device, 0x11)
                }
            }
            R.id.img_lock -> {
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline() == true) {
                    var state = device?.state
                    var lock = device?.lock
                    var x = TenTwoUtil.changeToTwo(state!!)
                    if (lock == 1) {
                        x[2] = 0
                    } else if (lock == 0) {
                        x[2] = 1
                    }
                    state = TenTwoUtil.changeToTen2(x)
                    device?.state = state
                    devicePresent?.sendBasic(mqService, device, 0x11)
                }
            }
            R.id.img_ai -> {
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline() == true) {
                    device?.mode=0x02
                    devicePresent?.sendBasic(mqService, device, 0x11)
                }
            }
            R.id.img_timer->{
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline()==true){
                    showTimerPop(0)
                }
            }
            R.id.tv_menu_timer -> {
                if (isDialogShow){
                    return
                }
                if (isDeviceOnline()==true){
                    showTimerPop(1)
                }
            }
            R.id.tv_menu_state -> {
                startActivity(DeviceStateActivity::class.java)
            }
            R.id.tv_menu_restore->{
                changeDialog(1)
            }
            R.id.tv_menu_set ->{
                var intent=Intent(this,AdvanceSetActivity::class.java)
                intent.putExtra("deviceMac",deviceMac)
                startActivityForResult(intent,100)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==100){
            var advanceArray=data.getIntArrayExtra("advanceArray")
            advanceArray[11]=0
            countTime.start()
            mqService?.sendAdvanceSet(deviceMac,advanceArray)
        }
    }
    private var changeDialog: ChangeDialog?=null
    private fun changeDialog(code:Int){
        if (changeDialog!=null && changeDialog?.isShowing==true){
            return
        }
        changeDialog= ChangeDialog(this)
        changeDialog?.setCanceledOnTouchOutside(false)
        if (code==1){
            changeDialog?.mode=1
            changeDialog?.title="恢复设置"
            changeDialog?.tips="确定回复设备出厂设置?"
        }
        changeDialog?.show()
        changeDialog?.setOnNegativeClickListener {
            changeDialog?.dismiss()
        }
        changeDialog?.setOnPositiveClickListener {
            changeDialog?.dismiss()
            if (isDeviceOnline()==true){
                devicePresent?.sendBasic(mqService,device,0x61)
            }
        }
        backgroundAlpha(0.6f)
        changeDialog?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }
    }

    private fun isDeviceOnline(): Boolean? {
        if (device?.online == false) {
            ToastUtils.toastShort(this, "设备已离线")
            devicePresent?.getData(mqService, deviceMac, 0x11)
        }
        return device?.online
    }

    fun setMode(device: Device?) {
        if (device!=null){
            var mode = device.mode
            var open = device.open
            var setTemp = device.setTemp
            var curTemp = device.currentTemp
            var lock = device.lock
            var online=device.online
            if (online){
                if (mode == 0x01) {
                    tv_state.text = "手动模式"
                    progressBar.isCanTouch = true
                    progressBar.mode = 0
                    progressBar.setCurrentAngle(setTemp)
                    img_hand.setImageResource(R.mipmap.img_mode_hand)
                    img_ai.setImageResource(R.mipmap.img_ai)
                    img_timer.setImageResource(R.mipmap.img_timer)
                } else if (mode == 0x02) {
                    tv_state.text = "智能模式"
                    img_hand.setImageResource(R.mipmap.img_hand)
                    img_ai.setImageResource(R.mipmap.img_mode_ai2)
                    img_timer.setImageResource(R.mipmap.img_timer)
                } else if (mode == 0x03) {
                    tv_state.text = "日定时模式"
                    progressBar.isCanTouch = false
                    progressBar.mode = 2
                    img_hand.setImageResource(R.mipmap.img_hand)
                    img_ai.setImageResource(R.mipmap.img_ai)
                    img_timer.setImageResource(R.mipmap.img_mode_timer2)
                } else if (mode == 0x04) {
                    tv_state.text = "周定时模式"
                    progressBar.isCanTouch = false
                    progressBar.mode = 2
                    img_hand.setImageResource(R.mipmap.img_hand)
                    img_ai.setImageResource(R.mipmap.img_ai)
                    img_timer.setImageResource(R.mipmap.img_mode_timer2)
                }
                if (open == 1) {
                    animationDrawable?.start()
                } else if (open == 0) {
                    progressBar.mode = 1
                    animationDrawable?.stop()
                }
                tv_set_value.text = "${setTemp}℃"
                tv_cur_value.text = "${curTemp}℃"
                if (lock == 1) {
                    img_lock.setImageResource(R.mipmap.img_mode_lock2)
                } else if (lock == 0) {
                    img_lock.setImageResource(R.mipmap.img_lock)
                }
            }else{
                tv_state.text = "设备离线"
                progressBar.mode=3
                animationDrawable?.stop()
            }
        }

    }

    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#1b1311")
    }

    var timePopupWindow: PopupWindow? = null

    private fun showTimerPop(code: Int) {
        if (timePopupWindow != null && timePopupWindow?.isShowing == true) {
            return
        }
        var view = View.inflate(this, R.layout.popup_timer, null)

        timePopupWindow = PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        timePopupWindow?.isFocusable = true
        timePopupWindow?.isOutsideTouchable = true
        timePopupWindow?.update()

        timePopupWindow?.animationStyle = R.style.Popupwindow
        timePopupWindow?.showAtLocation(tv_cur, Gravity.BOTTOM, 0, 0)
        backgroundAlpha(0.6f)
        timePopupWindow?.setOnDismissListener(object : PopupWindow.OnDismissListener {
            override fun onDismiss() {
                backgroundAlpha(1.0f)
                timePopupWindow?.dismiss()
            }
        })

        var rl_timer_day = view.rl_timer_day
        var rl_timer_week = view.rl_timer_week
        var img_timer_day = view.img_timer_day
        var img_timer_week = view.img_timer_week
        var btn_ensure = view.btn_ensure
        if (code == 1) {
            btn_ensure.visibility = View.GONE
            img_timer_day.visibility = View.GONE
            img_timer_week.visibility = View.GONE
            rl_timer_day.setOnClickListener {

                //                img_timer_day.setImageResource(R.mipmap.img_timer_select)
//                img_timer_week.setImageResource(R.mipmap.img_timer_unselect)
                var intent = Intent(this, TimerActivity::class.java)
                intent.putExtra("timer", 1)
                intent.putExtra("deviceMac",deviceMac)
                startActivity(intent)
                timePopupWindow?.dismiss()
            }
            rl_timer_week.setOnClickListener {
                //                img_timer_day.setImageResource(R.mipmap.img_timer_select)
//                img_timer_week.setImageResource(R.mipmap.img_timer_unselect)
                var intent = Intent(this, TimerActivity::class.java)
                intent.putExtra("timer", 2)
                intent.putExtra("deviceMac",deviceMac)
                startActivity(intent)
                timePopupWindow?.dismiss()
            }

        } else if (code == 0) {
            btn_ensure.visibility = View.VISIBLE
            var timer: Int = 0
            rl_timer_day.setOnClickListener {
                img_timer_day.setImageResource(R.mipmap.img_timer_select)
                img_timer_week.setImageResource(R.mipmap.img_timer_unselect)
                timer = 1
            }
            rl_timer_week.setOnClickListener {
                img_timer_day.setImageResource(R.mipmap.img_timer_unselect)
                img_timer_week.setImageResource(R.mipmap.img_timer_select)
                timer = 2
            }
            btn_ensure.setOnClickListener {
                if (timer == 1) {
                    device?.mode = 0x03
                    devicePresent?.sendBasic(mqService, device, 0x11)
                    timePopupWindow?.dismiss()
                } else if (timer == 2) {
                    device?.mode = 0x04
                    devicePresent?.sendBasic(mqService, device, 0x11)
                    timePopupWindow?.dismiss()
                }
            }
        }
    }

    var bind = false
    var mqService: MQService? = null
    private var connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder: MQService.LocalBinder = p1 as MQService.LocalBinder
            mqService = binder.service
//            if (mqService != null) {
//                var topicName = "warmer/${deviceMac}/set"
//                mqService?.subscribe(topicName, 1)
//                devicePresent?.getData(mqService, deviceMac, 0x11)
//            }
        }
    }
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            try {
                var action = intent.action
                if ("offline" == action) {
                    device?.online = false
                    ToastUtils.toastShort(this@DeviceActivity,"设备已离线")
                    deviceDao?.update(device)
                    setMode(device)
                }else {
                    var deviceMac2 = intent.getStringExtra("deviceMac")
                    if (deviceMac == deviceMac2) {
                        var reset=intent.getIntExtra("reset",0)
                        if (reset==1){
                            setResult(1000)
                            finish()
                        }else{
                            var device2: Device? = intent.getSerializableExtra("device") as Device
                            device = device2
                            setMode(device)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
