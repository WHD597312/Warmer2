package com.peihou.warmer.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.peihou.warmer.R
import com.peihou.warmer.glide.GlideUtils
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils
import com.peihou.warmer.pojo.UserInfo
import kotlinx.android.synthetic.main.activity_test.*
import org.json.JSONObject
import java.util.HashMap

class TestActivity : AppCompatActivity() {

    var unbinder: Unbinder? = null
    var phone: String? = "18857428766"
    var password: String? = "123456"
    //    var map:MutableMap<String,Any?>?= mutableMapOf()
//    var map:Map<String,Any>=Ha
    var map: MutableMap<String?, Any?> = mutableMapOf()
//    var map2:Map<String,Any> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        unbinder = ButterKnife.bind(this)

        GlideUtils.loadCircleImg(this,
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1558935612290&di=85d0c05bfb9a5d68cb4665016cf14074&imgtype=0&src=http%3A%2F%2Fdingyue.ws.126.net%2F2019%2F04%2F26%2F75c5777887314070a359af411c493e5b.jpeg",
                imageView)

//        var map<String,Any>= mutableMapOf()
    }

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
        Log.i("onActivityResult", "-->$requestCode,$resultCode")
        var intentIntegrator = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentIntegrator != null) {
            if (intentIntegrator.contents == null) {
                Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show()
                var content = intentIntegrator.contents
                Log.i("scan", "-->$content")
            }
        }
    }

    private fun scanQrCode() {
        IntentIntegrator(this)
                .setOrientationLocked(true)
                .setCaptureActivity(ScanActivity::class.java)
                .setBeepEnabled(true)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setRequestCode(1001)
                .initiateScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder?.unbind()
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
}
