package com.peihou.warmer.activity

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import butterknife.OnClick
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.present.DevicePersentImpl
import com.peihou.warmer.mvp.present.IDevicePersent
import com.peihou.warmer.mvp.present.IUserPresent
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.TenTwoUtil
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_set01.*


class Set01Activity : BaseActivity(), IUserView {
    companion object {
        var running=false
    }
    override fun fail(code: Int) {

    }

    override fun success(code: Int) {
        countTime.start()
    }


    var position: Int? = 0
    var deviceMac: String? = null
    override fun initParms(parms: Bundle?) {
        position = parms?.getInt("position")
        deviceMac = parms?.getString("deviceMac")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_set01
    }

    var deviceDaoImpl: DeviceDaoImpl?=null
    var devicePresent: IDevicePersent = DevicePersentImpl(this)
    private var advanceArray: IntArray = IntArray(15)
    var changeValue: Int = 0
    override fun initView() {
        seekbar.setIndicatorTextDecimalFormat("0")
        if (position == 2) {
            seekbar.setRange(-9f, 9f)
            tv_name.text = "温度校正"
            tv_tip.visibility = View.GONE
        } else if (position == 3) {
            seekbar.setRange(5f, 60f)
            tv_name.text = "内置传感器的设置范围"
            tv_tip.visibility = View.GONE
        } else if (position == 4) {
            seekbar.setRange(20f, 80f)
            tv_name.text = "外置传感器的设置范围"
            tv_tip.visibility = View.GONE
        } else if (position == 5) {
            seekbar.setRange(20f, 80f)
            tv_name.text = "外置传感器限温设置点"
            tv_tip.visibility = View.GONE
        } else if (position == 6) {
            seekbar.setRange(1f, 10f)
            tv_name.text = "输出延时时间"
            tv_tip.visibility = View.GONE
            tv_flag.text = "分钟"
        }
        deviceDaoImpl=application.deviceDao
        var service = Intent(this, MQService::class.java)
        bind = bindService(service, connnect, Context.BIND_AUTO_CREATE)
        var filter = IntentFilter("Set01Activity")
        filter.addAction("offline")
        registerReceiver(receiver, filter)

        seekbar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
                changeValue = Math.round(leftValue)
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

                tv_value.text = "$changeValue"
                when (position) {
                    0 ->advanceArray[0]=changeValue
                    3->advanceArray[2]=changeValue
                    4->advanceArray[3]=changeValue
                    5->advanceArray[4]=changeValue
                    2->{
                        if (changeValue<0){
                            var  value=Math.abs(changeValue)
                            var x=TenTwoUtil.changeToTwo(value)
                            x[0]=1
                            value=TenTwoUtil.changeToTen2(x)
                            advanceArray[5]=value
                        }else if (changeValue>=0){
                            advanceArray[5]=changeValue
                        }
                    }
                    6->advanceArray[6]=changeValue
                }
            }

        })
    }

    @OnClick(R.id.img_back, R.id.btn_submit)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> {
                var intent=Intent()
                intent.putExtra("advanceArray",advanceArray)
                setResult(100,intent)
                finish()
            }
            R.id.btn_submit -> {
                if (dialogLoad.isShowing)
                    ToastUtils.toastShort(this, "请稍后...")
                else {
                    var success=mqService?.sendAdvanceSet(deviceMac, advanceArray)
                    if (success==true){
                        countTime.start()
                    }else{
                        ToastUtils.toastShort(this, "当前网络不可用")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        var intent=Intent()
        intent.putExtra("advanceArray",advanceArray)
        setResult(100,intent)
        super.onBackPressed()
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
                devicePresent.getData(mqService, deviceMac, 0x41)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        running=false
    }
    fun setMode() {
        when (position) {
            0 -> {
                tv_value.text = "${advanceArray[0]}"
                seekbar.setValue(advanceArray[0].toFloat())
            }
            3 -> {
                tv_value.text = "${advanceArray[2]}"
                seekbar.setValue(advanceArray[2].toFloat())
            }
            4->{
                tv_value.text = "${advanceArray[3]}"
                seekbar.setValue(advanceArray[3].toFloat())
            }
            5->{
                tv_value.text = "${advanceArray[4]}"
                seekbar.setValue(advanceArray[4].toFloat())
            }
            2->{
                var x=TenTwoUtil.changeToTwo(advanceArray[5])
                var value=TenTwoUtil.changeToTen2(x)
                if (x[0]==1){
                    x[0]=0
                    value=-TenTwoUtil.changeToTen2(x)
                }
                tv_value.text = "$value"
                seekbar.setValue(value.toFloat())
            }
            6->{
                tv_value.text = "${advanceArray[6]}"
                seekbar.setValue(advanceArray[6].toFloat())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bind)
            unbindService(connnect)
        unregisterReceiver(receiver)
    }

    var mqService: MQService? = null
    var bind = false
    private var connnect = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder: MQService.LocalBinder = p1 as MQService.LocalBinder
            mqService = binder.service
            devicePresent.getData(mqService, deviceMac, 0x41)
        }
    }

    var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            try {
                if ("offline" == intent.action) {
                    ToastUtils.toastShort(this@Set01Activity,"设备已离线")
                }else {
                    var deviceMac2 = intent.getStringExtra("deviceMac")
                    if (deviceMac2 == deviceMac) {
                        var reset = intent.getIntExtra("reset", 0)
                        if (reset == 1) {
                            var intent2=Intent(this@Set01Activity,MainActivity::class.java)
                            intent2.putExtra("reset",1)
                            startActivity(intent2)
                        } else {
                            advanceArray = intent.getIntArrayExtra("advanceArray")
                            setMode()
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}
