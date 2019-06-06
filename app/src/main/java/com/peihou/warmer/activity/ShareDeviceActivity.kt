package com.peihou.warmer.activity

import android.os.Bundle
import android.view.View
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.utils.ZXingUtils
import kotlinx.android.synthetic.main.activity_share_device.*

class ShareDeviceActivity : BaseActivity() {
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_share_device
    }

    override fun initView() {
       createQrCode()
    }


    /**生成二维码 */
    private fun createQrCode() {
        val bitmap = ZXingUtils.createQRImage("ssssss", 1000, 1000)
        img_qrCode.setImageBitmap(bitmap)
//        mBitmap = bitmap
    }
}
