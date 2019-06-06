package com.peihou.warmer.activity

import android.content.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.glide.GlideUtils
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils
import com.peihou.warmer.pojo.UserInfo
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ZXingUtils
import kotlinx.android.synthetic.main.activity_test.*
import me.jessyan.autosize.internal.CustomAdapt
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.HashMap

class TestActivity : BaseActivity(),CustomAdapt{
    override fun isBaseOnWidth(): Boolean {
        return false
    }

    override fun getSizeInDp(): Float {
        return 640f
    }

    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_test
    }

    override fun initView() {
        var service=Intent(this,MQService::class.java)
        var filter=IntentFilter("sss")
        registerReceiver(receiver,filter)
        bind=bindService(service,connection, Context.BIND_AUTO_CREATE)
//        createQrCode()
        GlideUtils.loadCircleImg(this,
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1558935612290&di=85d0c05bfb9a5d68cb4665016cf14074&imgtype=0&src=http%3A%2F%2Fdingyue.ws.126.net%2F2019%2F04%2F26%2F75c5777887314070a359af411c493e5b.jpeg",
                imageView)
    }


    private var mBitmap: Bitmap?=null
    /**生成二维码 */
//    private fun createQrCode() {
//        var share="123456gggggggggaffadssssssdddddddddddddssssssssssssssssssssssssssssssssssssssss"
//        share = String(Base64.encode(share.toByteArray(charset("utf-8")), Base64.NO_WRAP), Charset.forName("UTF-8"))
//        val bitmap = ZXingUtils.createQRImage(share, 1000, 1000)
//        img_qrCode.setImageBitmap(bitmap)
//        mBitmap = bitmap
//    }

    var phone: String? = "18857428766"
    var password: String? = "123456"
    //    var map:MutableMap<String,Any?>?= mutableMapOf()
//    var map:Map<String,Any>=Ha
    var map: MutableMap<String?, Any?> = mutableMapOf()
//    var map2:Map<String,Any> = HashMap()


//    @OnClick({R.id.button})
//    fun onClick(view: View):Unit{
//        when(view.id){
//            R.id.button->Toast.makeText(this,"ssss",Toast.LENGTH_SHORT).show()
//        }
//    }

    @OnClick(R.id.button, R.id.button2, R.id.button5)
    fun onClick(view: View) {
        when (view.id) {
            R.id.button -> {
                map["phone"] = phone
                map["password"] = password
                LoginAsync(this).execute(map)
//                map.put("phone",phone)
            }
            R.id.button2 -> {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(this, notification)
                r.play()
            }
            R.id.button5 -> {
                scanQrCode()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        IntentIntegrator.parseActivityResult(requestCode,resultCode,intent)
//        var bundle:Bundle?=data?.extras
//        var keySet:MutableSet<String>?=bundle?.keySet()
//        Log.i("ActivityResult","--$keySet")
//        keySet?.forEach {
//            var key=it
//            var value=bundle?.getString("SCAN_RESULT")
//            Log.i("scan","-->$key,$value")
//        }
        if (requestCode==100){
            var scanResult=data?.getStringExtra("SCAN_RESULT")
            if (!TextUtils.isEmpty(scanResult)){
                Toast.makeText(this, scanResult, Toast.LENGTH_SHORT).show()
            }
//            var intentIntegrator = IntentIntegrator.parseActivityResult(rresultCode,intent)
//            if (intentIntegrator != null) {
//                if (intentIntegrator.contents == null) {
//                    Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show()
//                    var content = intentIntegrator.contents
//                    Log.i("scan", "-->$content")
//                }
//            }
        }
//        var intentIntegrator = IntentIntegrator.parseActivityResult(resultCode,intent)
//        if (intentIntegrator != null) {
//            if (intentIntegrator.contents == null) {
//                Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show()
//                var content = intentIntegrator.contents
//                Log.i("scan", "-->$content")
//            }
//        }
    }

    private fun scanQrCode() {
        IntentIntegrator(this)
                .setOrientationLocked(true)
                .setCaptureActivity(ScanActivity::class.java)
                .setRequestCode(100)
                .initiateScan()
    }



    //    class LoginAsync :AsyncTask<MutableMap<>{
//        constructor()
//        override fun doInBackground(vararg p0: MutableMap<String, Any>?): Int {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//    }
    class LoginAsync(activity: TestActivity) : BaseWeakAsyncTask<MutableMap<String?, Any?>, Void?, Int, TestActivity?>(activity) {
        override fun doInBackground(target: TestActivity?, vararg params: MutableMap<String?, Any?>): Int {
            var code = 0
            var url = HttpUtils.ipAddress + "user/login"
            var params2 = params[0]
            var result: String = HttpUtils.requestPost(url, params2)
            if (TextUtils.isEmpty(result)) {
                result = HttpUtils.requestPost(url, params2)
            }
            if (!TextUtils.isEmpty(result)) {
                var jsonObject = JSONObject(result)

                var returnData = jsonObject.getJSONObject("returnData")
                var data = returnData.toString()
                var gson = Gson()
                var userInfo = gson.fromJson(data, UserInfo::class.java)
                Log.i("LoginAsync", "-->" + userInfo)
            }

            return code
        }

        override fun onPostExecute(target: TestActivity?, result: Int?) {

        }

    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)

        if (bind)
            unbindService(connection)
    }
    var mqService:MQService?=null
    var bind:Boolean=false

    private var connection:ServiceConnection=object :ServiceConnection{
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as MQService.LocalBinder
            mqService=binder.service

        }

    }
    private var receiver:BroadcastReceiver=object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

        }
    }
}
