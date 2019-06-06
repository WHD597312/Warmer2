package com.peihou.warmer.activity

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.peihou.warmer.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.peihou.warmer.base.BaseActivity
import kotlinx.android.synthetic.main.activity_scan.*


class ScanActivity : BaseActivity(),DecoratedBarcodeView.TorchListener {
    var savedInstanceState:Bundle?=null
    override fun initParms(parms: Bundle?) {
        savedInstanceState=parms
    }

    override fun bindLayout(): Int {
        return R.layout.activity_scan
    }

    override fun initView() {
        dbv.setStatusText("ssssss")
        dbv.setTorchListener(this)
        captureManager = CaptureManager(this, dbv)
        captureManager?.initializeFromIntent(intent, savedInstanceState)
        captureManager?.decode()
    }

    private var isLightOn:Boolean=false
    override fun onTorchOn() {
        isLightOn=true
    }

    override fun onTorchOff() {
        isLightOn=false
    }

    private fun hasFlash():Boolean{
        return applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }
    private var captureManager:CaptureManager?=null


//    override fun onSaveInstanceState(outState: Bundle?) {
//        super.onSaveInstanceState(outState)
//        captureManager?.onSaveInstanceState(outState)
//    }

    @OnClick(R.id.btn_light)
    fun onClick(view: View){
        when(view.id){
            R.id.btn_light->{
                if (isLightOn){
                    dbv.setTorchOff()
                }else{
                    dbv.setTorchOn()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        captureManager?.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureManager?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager?.onDestroy()
    }
}
