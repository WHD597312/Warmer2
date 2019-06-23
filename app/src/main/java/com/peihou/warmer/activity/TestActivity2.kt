package com.peihou.warmer.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.service.MQService
import kotlinx.android.synthetic.main.activity_set04.*
import java.util.*

class TestActivity2 : BaseActivity() {
    private var deviceMac:String="123456789"

    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_test2
    }
    override fun initView() {
        var service=Intent(this,MQService::class.java)
        bind=bindService(service,conn, Context.BIND_AUTO_CREATE)

    }

    var state:Int=0
    @OnClick(R.id.btn_basic,R.id.btn_hand,R.id.btn_lock,R.id.btn_ai,R.id.btn_timer,R.id.btn_timer2,R.id.btn_store,R.id.btn_day,R.id.btn_week)
    fun onClick(view:View){
        when(view.id){
            R.id.btn_basic->{
                if (state==0){
                    state=1
                    sendBasic(64,0)
                }else if (state==1){
                    state=0
                    sendBasic(128,0)
                }
            }
            R.id.btn_hand->{
                sendBasic(64,0x01)
            }
            R.id.btn_lock->{
                sendBasic(160,0x02)
            }
            R.id.btn_ai->sendBasic(64,0x02)
            R.id.btn_timer->sendBasic(64,0x03)
            R.id.btn_timer2->sendBasic(64,0x04)
            R.id.btn_store->{
                state=0
                sendBasic(64,0)
            }
            R.id.btn_day->dayPlan()
            R.id.btn_week->weekPlan()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (bind)
            unbindService(conn)
    }

    private var mqService:MQService?=null
    private var bind=false
    private var conn=object :ServiceConnection{
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var binder:MQService.LocalBinder= p1 as MQService.LocalBinder
            mqService=binder.service
        }
    }

    private fun sendBasic(state :Int,mode:Int){
        var topicName="warmer/${deviceMac}/transfer"
        var bytes=ByteArray(16)
        try {
            bytes[0]= 0x90.toByte()
            bytes[1]=0x11
            bytes[2]=0x0d
            bytes[3]=mode.toByte()
            bytes[4]=state.toByte()
            bytes[5]=25
            bytes[6]=30
            var sum=0
            for (i in 0.. 6)
                sum+=bytes[i]
            bytes[14]=(sum%256).toByte()
            bytes[15]=0x46
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var success=mqService?.publish(topicName,1,bytes)
        Log.i("Basic","-->${success}")
    }
    private fun dayPlan(){
        var topicName="warmer/${deviceMac}/transfer"
        var bytes=ByteArray(45)
        try {
            bytes[0]= 0x90.toByte()
            bytes[1]=0x21
            bytes[2]=0x2a
            var index=2
            for (i in 0 ..11){
                bytes[++index]= random.nextInt(43).toByte()
                bytes[++index]= random.nextInt(24).toByte()
                bytes[++index]= random.nextInt(60).toByte()
            }
            var sum=0
            for (i in 0.. 42)
                sum+=bytes[i]
            bytes[43]= (sum%256).toByte()
            bytes[44]=0x46
            var success=mqService?.publish(topicName,1,bytes)
            Log.i("dayPlan","-->${success}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    var random=Random()
    private fun weekPlan(){
        var topicName="warmer/$deviceMac/transfer"
        var bytes=ByteArray(93)
        try {
            bytes[0]= 0x90.toByte()
            bytes[1]=0x31
            bytes[2]=0x5a
            var index=2
            for (i in 0..6){
                for (j in 0..3){
                    bytes[++index]= random.nextInt(43).toByte()
                    bytes[++index]= random.nextInt(24).toByte()
                    bytes[++index]= random.nextInt(60).toByte()
                }
            }
            var sum=0
            for (i in 0.. 90)
                sum+=bytes[i]
            bytes[91]= (sum%256).toByte()
            bytes[92]=0x46
            var success=mqService?.publish(topicName,1,bytes)
            Log.i("dayPlan","-->$success")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
