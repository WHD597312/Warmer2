package com.peihou.warmer.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.donkingliang.imageselector.utils.ImageSelector
import com.peihou.warmer.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.utils.ZXingUtils
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.File


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

    @OnClick(R.id.btn_light,R.id.btn_gallery)
    fun onClick(view: View){
        when(view.id){
            R.id.btn_light->{
                if (isLightOn){
                    dbv.setTorchOff()
                }else{
                    dbv.setTorchOn()
                }
            }
            R.id.btn_gallery->{
//                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
//                        .setFileProviderAuthority(Constants.fileProvider)
//                        .setCount(1)
//                        .start(Constants.PIC);
                startGallery()
            }
        }
    }

    val icon=2
    //打开相册
    fun startGallery() {
        ImageSelector.builder()
                .useCamera(false) // 设置是否使用拍照
                .setCrop(false)  // 设置是否使用图片剪切功能。
                .setSingle(true)  //设置是否单选
                .setViewImage(false) //是否点击放大图片查看,，默认为true
                .start(this, icon) // 打开相册
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==icon){
            val images = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT)
            if (images != null && images.size > 0) {
                val path = images[0]
                var bitmap=BitmapFactory.decodeFile(path)
                var result=ZXingUtils.parseQRcode(bitmap)
                Log.i("ParseZxing","-->$result")
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
