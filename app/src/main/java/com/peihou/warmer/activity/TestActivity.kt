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
import com.google.gson.Gson
import com.peihou.warmer.R
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils
import com.peihou.warmer.pojo.UserInfo
import kotlinx.android.synthetic.main.activity_test.*
import org.json.JSONObject
import java.util.HashMap

class TestActivity : AppCompatActivity() {

    var unbinder: Unbinder? = null
    var phone:String?="18857428766"
    var password:String?="123456"
//    var map:MutableMap<String,Any?>?= mutableMapOf()
//    var map:Map<String,Any>=Ha
    var map: MutableMap<String?, Any?> = mutableMapOf()
//    var map2:Map<String,Any> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        unbinder = ButterKnife.bind(this)
//        var map<String,Any>= mutableMapOf()
    }

//    @OnClick({R.id.button})
//    fun onClick(view: View):Unit{
//        when(view.id){
//            R.id.button->Toast.makeText(this,"ssss",Toast.LENGTH_SHORT).show()
//        }
//    }

    @OnClick(R.id.button,R.id.button2)
    fun onClick(view: View) {
        when(view.id){
            R.id.button-> {
                map["phone"]=phone
                map["password"]=password
                LoginAsync(this).execute(map)
//                map.put("phone",phone)
            }
            R.id.button2->{
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(this, notification)
                r.play()
            }
        }
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
    class LoginAsync(activity: TestActivity) :BaseWeakAsyncTask<MutableMap<String?,Any?>,Void?,Int,TestActivity?>(activity){
    override fun doInBackground(target: TestActivity?, vararg params: MutableMap<String?, Any?>): Int {
        var code=0
        var  url=HttpUtils.ipAddress+"user/login"
        var params=params[0]
        var result:String=HttpUtils.requestPost(url,params)
        if (TextUtils.isEmpty(result)) {
            result = HttpUtils.requestPost(url, params)
        }
        if (!TextUtils.isEmpty(result)) {
            var jsonObject=JSONObject(result)

            var  returnData=jsonObject.getJSONObject("returnData")
            var data=returnData.toString()
            var gson=Gson()
            var userInfo=gson.fromJson(data,UserInfo::class.java)
            Log.i("LoginAsync","-->"+userInfo)
        }

        return code
    }

    override fun onPostExecute(target: TestActivity?, result: Int?) {

    }

}
}
