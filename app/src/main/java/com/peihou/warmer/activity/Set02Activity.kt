package com.peihou.warmer.activity

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.mvp.present.DevicePersentImpl
import com.peihou.warmer.mvp.present.IDevicePersent
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_set02.*

class Set02Activity : BaseActivity(),IUserView {
    override fun fail(code: Int) {

    }

    override fun success(code: Int) {
        countTime.start()
    }

    companion object {
        var running=false
    }
    var deviceMac:String?=null
    override fun initParms(parms: Bundle?) {
        deviceMac=parms?.getString("deviceMac")
    }

    override fun bindLayout(): Int {
       return R.layout.activity_set02
    }

    var deviceDaoImpl:DeviceDaoImpl?=null
    var devicePresent: IDevicePersent = DevicePersentImpl(this)
    override fun initView() {
        deviceDaoImpl=application.deviceDao
        var service= Intent(this,MQService::class.java)
        bind=bindService(service,connnect, Context.BIND_AUTO_CREATE)
        var filter=IntentFilter("Set01Activity")
        filter.addAction("offline")
        registerReceiver(receiver,filter)
    }

    override fun onStart() {
        super.onStart()
        running =true
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

    override fun onBackPressed() {
        var intent=Intent()
        intent.putExtra("advanceArray",advanceArray)
        setResult(100,intent)
        super.onBackPressed()
    }
    @OnClick(R.id.img_back,R.id.rl_layout01,R.id.rl_layout02,R.id.rl_layout03,R.id.btn_submit)
    fun onClick(view:View){
        when(view.id){
            R.id.img_back->{
                var intent=Intent()
                intent.putExtra("advanceArray",advanceArray)
                setResult(100,intent)
                finish()
            }
            R.id.rl_layout01->{
                if (selectPosition!=1){
                    selectPosition=1
                    setMode()
                }
            }
            R.id.rl_layout02->{
                if (selectPosition!=2){
                    selectPosition=2
                    setMode()
                }
            }
            R.id.rl_layout03->{
                if (selectPosition!=3){
                    selectPosition=3
                    setMode()
                }
            }
            R.id.btn_submit->{
                if (dialogLoad.isShowing){
                    ToastUtils.toastShort(this,"请稍后...")
                }else{
                    advanceArray[1]=selectPosition
                    var success=mqService?.sendAdvanceSet(deviceMac,advanceArray)
                    if (success==true){
                        countTime.start()
                    }else{
                        ToastUtils.toastShort(this, "当前网络不可用")
                    }
                }
            }
        }
    }
    var selectPosition:Int=1
    private fun setMode(){
        when(selectPosition){
            1->{
                img_system01.setImageResource(R.mipmap.img_system)
                img_system02.setImageResource(0)
                img_system03.setImageResource(0)
            }
            2->{
                img_system01.setImageResource(0)
                img_system02.setImageResource(R.mipmap.img_system)
                img_system03.setImageResource(0)
            }
            3->{
                img_system01.setImageResource(0)
                img_system02.setImageResource(0)
                img_system03.setImageResource(R.mipmap.img_system)
            }
        }
    }
    var advanceArray:IntArray= IntArray(15)
    var mqService: MQService?=null
    var bind=false
    private var connnect=object: ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder: MQService.LocalBinder= p1 as MQService.LocalBinder
            mqService=binder.service
            mqService?.getData(deviceMac,0x41)
            countTime.start()
        }
    }

    var receiver=object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent) {
            if ("offline"==intent.action){
                ToastUtils.toastShort(this@Set02Activity,"设备已离线")
            }else{
                var deviceMac2=intent.getStringExtra("deviceMac")

                if (deviceMac2==deviceMac){
                    var reset = intent.getIntExtra("reset", 0)
                    if (reset == 1) {
                        var intent2=Intent(this@Set02Activity,MainActivity::class.java)
                        intent2.putExtra("reset",1)
                        startActivity(intent2)
                    }else{
                        advanceArray=intent.getIntArrayExtra("advanceArray")
                        selectPosition=advanceArray[1]
                        setMode()
                    }
                }
            }
        }
    }
}
