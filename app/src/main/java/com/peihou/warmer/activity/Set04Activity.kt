package com.peihou.warmer.activity


import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_set04.*


class Set04Activity : BaseActivity() {
    companion object {
        var running=false
    }
    var position: Int? = null
    var deviceMac:String?=null
    override fun initParms(parms: Bundle?) {
        position=parms?.getInt("position")
        deviceMac=parms?.getString("deviceMac")
    }

    override fun bindLayout(): Int {
        return R.layout.activity_set04
    }

    var deviceDaoImpl:DeviceDaoImpl?=null
    override fun initView() {
        deviceDaoImpl=application.deviceDao
        if (position==8){
            tv_name.text="掉电记忆"
        }else if (position==9){
            tv_name.text="输出设置"
            tv_out.text="输出设置"
            tv_tip.visibility=View.VISIBLE
        }
        var service=Intent(this,MQService::class.java)
        bind=bindService(service,con, Context.BIND_AUTO_CREATE)
        var filter=IntentFilter("Set01Activity")
        filter.addAction("offline")
        registerReceiver(receiver, filter)
    }

    override fun onStart() {
        super.onStart()
        running=true
        if (mqService!=null){
            var device=deviceDaoImpl?.findDevice(deviceMac)
            if (device==null){
                var intent2=Intent(this@Set04Activity,MainActivity::class.java)
                intent2.putExtra("reset",1)
                startActivity(intent2)
            }else{
                countTime.start()
                mqService?.getData(deviceMac,0x41)
            }

        }
    }

    override fun onStop() {
        super.onStop()
        running=false
    }

    override fun onBackPressed() {
        var intent=Intent()
        intent.putExtra("advanceArray",advanceArray)
        setResult(100,intent)
        super.onBackPressed()
    }
    @OnClick(R.id.img_back,R.id.btn_submit,R.id.img_state)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back ->{
                var intent=Intent()
                intent.putExtra("advanceArray",advanceArray)
                setResult(100,intent)
                finish()
            }
            R.id.img_state->{
                if (position==8){
                    if (advanceArray[8]==0){
                        advanceArray[8]=1
                        tv_value.text="打开"
                        img_state.setImageResource(R.mipmap.img_open)
                    }else if (advanceArray[8]==1){
                        advanceArray[8]=0
                        tv_value.text="关闭"
                        img_state.setImageResource(R.mipmap.img_close)
                    }
                }else if (position==9){
                    if (advanceArray[11]==0){
                        advanceArray[11]=1
                        tv_value.text="打开"
                        img_state.setImageResource(R.mipmap.img_open)
                    }else if (advanceArray[11]==1){
                        advanceArray[11]=0
                        tv_value.text="关闭"
                        img_state.setImageResource(R.mipmap.img_close)
                    }
                }
            }
            R.id.btn_submit->{
                var success=mqService?.sendAdvanceSet(deviceMac,advanceArray)
                if (success==true){
                    countTime.start()
                }else{
                    ToastUtils.toastShort(this,"当前网络不可用")
                }
            }
        }
    }

    private fun setMode(){
        if (position==8){
            if (advanceArray[8]==0){
                tv_value.text="关闭"
                img_state.setImageResource(R.mipmap.img_close)
            }else if (advanceArray[8]==1) {
                tv_value.text = "打开"
                img_state.setImageResource(R.mipmap.img_open)
            }
        }else if (position==9){
            if (advanceArray[11]==0){
                tv_value.text="关闭"
                img_state.setImageResource(R.mipmap.img_close)
            }else if (advanceArray[11]==1){
                tv_value.text = "打开"
                img_state.setImageResource(R.mipmap.img_open)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (bind)
            unbindService(con)
        unregisterReceiver(receiver)
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
    var advanceArray:IntArray= IntArray(15)
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            if ("offline" == intent.action) {
                ToastUtils.toastShort(this@Set04Activity,"设备已离线")
            } else {
                var deviceMac2 = intent.getStringExtra("deviceMac")
                if (deviceMac == deviceMac2) {
                    var reset = intent.getIntExtra("reset", 0)
                    if (reset == 1) {
                        var intent2 = Intent(this@Set04Activity, MainActivity::class.java)
                        intent2.putExtra("reset", 1)
                        startActivity(intent2)
                    }else {
                        advanceArray = intent.getIntArrayExtra("advanceArray")
                        setMode()
                    }
                }
            }
        }

    }

}
