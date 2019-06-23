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
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_set03.*

class Set03Activity : BaseActivity() {
    companion object {
        var running = false
    }

    private var deviceMac: String? = null
    override fun initParms(parms: Bundle?) {
        deviceMac = parms?.getString("deviceMac")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_set03
    }

    var leftValue = 5
    var rightValue = 10
    var deviceDaoImpl:DeviceDaoImpl?=null
    override fun initView() {
        deviceDaoImpl=application.deviceDao
        seekbar.setIndicatorTextDecimalFormat("0")
        seekbar.setValue(5f, 10f)
        seekbar.setRange(5f, 10f, 1f)
        var service = Intent(this, MQService::class.java)
        bind = bindService(service, con, Context.BIND_AUTO_CREATE)
        var filter = IntentFilter("Set01Activity")
        filter.addAction("offline")
        registerReceiver(receiver, filter)
        seekbar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onRangeChanged(view: RangeSeekBar?, left: Float, right: Float, isFromUser: Boolean) {
                leftValue = Math.round(left)
                rightValue = Math.round(right)
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                tv_left.text = "$leftValue"
                tv_right.text = "$rightValue"
                advanceArray[9] = leftValue
                advanceArray[10] = rightValue
            }

        })
    }

    override fun onStart() {
        super.onStart()
        running = true
        if (mqService != null) {
            var device=deviceDaoImpl?.findDevice(deviceMac)
            if (device==null){
                var intent2=Intent(this@Set03Activity,MainActivity::class.java)
                intent2.putExtra("reset",1)
                startActivity(intent2)
            }else{
                countTime.start()
                mqService?.getData(deviceMac, 0x41)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        running = false
    }

    override fun onBackPressed() {
        var intent=Intent()
        intent.putExtra("advanceArray",advanceArray)
        setResult(100,intent)
        super.onBackPressed()
    }

    @OnClick(R.id.img_back, R.id.img_state, R.id.btn_submit)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> {
                var intent=Intent()
                intent.putExtra("advanceArray",advanceArray)
                setResult(100,intent)
                finish()
            }
            R.id.img_state -> {
                if (advanceArray[7]==0){
                    advanceArray[7]=1
                    img_state.setImageResource(R.mipmap.img_open)
                }else if (advanceArray[7]==1){
                    advanceArray[7]=0
                    img_state.setImageResource(R.mipmap.img_close)
                }
            }
            R.id.btn_submit -> {
                if (isDialogShow) {
                    ToastUtils.toastShort(this, "请稍后...")
                } else {
                    var  success=mqService?.sendAdvanceSet(deviceMac, advanceArray)
                    if (success==true){
                        countTime.start()
                    }else{
                        ToastUtils.toastShort(this,"当前网络不可用")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bind)
            unbindService(con)
        unregisterReceiver(receiver)
    }

    private var advanceArray: IntArray = IntArray(15)
    private fun setMode() {
        if (advanceArray[7] == 0)
            img_state.setImageResource(R.mipmap.img_close)
        else if (advanceArray[7] == 1)
            img_state.setImageResource(R.mipmap.img_open)
        tv_left.text = "${advanceArray[9]}"
        tv_right.text = "${advanceArray[10]}"
        seekbar.setValue(advanceArray[9].toFloat(), advanceArray[10].toFloat())
        tv_right.text = "${advanceArray[10]}"
    }

    private var mqService: MQService? = null
    private var bind = false
    private var con = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder: MQService.LocalBinder = p1 as MQService.LocalBinder
            mqService = binder.service
            mqService?.getData(deviceMac, 0x41)
        }
    }
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            if ("offline" == intent.action) {
                ToastUtils.toastShort(this@Set03Activity,"设备已离线")
            } else {
                var deviceMac2 = intent.getStringExtra("deviceMac")
                if (deviceMac == deviceMac2) {
                    var reset = intent.getIntExtra("reset", 0)
                    if (reset == 1) {
                        var intent2 = Intent(this@Set03Activity, MainActivity::class.java)
                        intent2.putExtra("reset", 1)
                        startActivity(intent2)
                    } else{
                        advanceArray = intent.getIntArrayExtra("advanceArray")
                        setMode()
                    }
                }
            }
        }
    }
}
