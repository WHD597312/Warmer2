package com.peihou.warmer

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils


import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils


import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    internal var map: MutableMap<String, Any> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            //            List<> exams=new ArrayList<>();
            val jsonArray = JSONArray()
            val jsonObject = JSONObject()
            jsonObject["examId"] = 1
            jsonObject["examName"] = "11"
            val jsonObject2 = JSONObject()
            jsonObject2["examId"] = 2
            jsonObject2["examName"] = "22"
            val jsonObject3 = JSONObject()
            jsonObject3["examId"] = 3
            jsonObject3["examName"] = "33"
            jsonArray.add(jsonObject)
            jsonArray.add(jsonObject2)
            jsonArray.add(jsonObject3)
            //            jsonArray.add(JSON.toJSONString(new Exam(2,"22")));
            //            jsonArray.add(JSON.toJSONString(new Exam(3,"33")));
            val map = HashMap<String, Any>()

            //            jsonArray.add(new Exam(4,"44"));
            //            jsonArray.add(new Exam(5,"55"));
            //            for (int i = 0; i <exams.size(); i++) {
            //               String ss=JSON.toJSONString(exams.get(i));
            //               jsonArray.add(ss);
            //            }
            map["exam"] = jsonArray
            val jsonObject4 = JSONObject()
            jsonObject4["exam"] = map["exam"]
            val ss = jsonObject4.toJSONString()
            println("sss=$ss")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        map["sss"] = 111
        LoginAsync(this).execute(map)
    }

    internal inner class LoginAsync(activity: Activity) : BaseWeakAsyncTask<Map<String, Any>, Void, Int, Activity>(activity) {

        override fun doInBackground(activity: Activity, vararg maps: Map<String, Any>): Int? {
            val url = HttpUtils.ipAddress + "user/login"
            val params = maps[0]
            var result = HttpUtils.requestPost(url, params)
            if (TextUtils.isEmpty(result)) {
                result = HttpUtils.requestPost(url, params)
            }
            if (!TextUtils.isEmpty(result)) {

            }
            return null
        }

        override fun onPostExecute(activity: Activity, integer: Int?) {

        }
    }
}
