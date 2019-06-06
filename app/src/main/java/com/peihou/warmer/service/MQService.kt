package com.peihou.warmer.service

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.os.AsyncTask
import android.os.Binder
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.util.Log

import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.pojo.Device

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class MQService : IntentService("MQService") {


    private val TAG = "MQService"
    private val host = "tcp://47.111.101.184:1883"//mqtt连接服务端ip
    private val userName = "mosquitto"//mqtt连接用户名
    private val passWord = "mosquitto"//mqtt连接密码
    //    private static final android.os.Handler mainThreadHandler = new android.os.Handler(Looper.getMainLooper());

    private var client: MqttClient? = null//mqtt客户端

    var myTopic = "rango/dc4f220aa96e/transfer"

    private var options: MqttConnectOptions? = null

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var clientId: String?=null
    private var binder = LocalBinder()
    val topicNames: List<String>
        get() {
            val topicNames = ArrayList<String>()
            topicNames.add(myTopic)
            return topicNames
        }


    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION === action) {
                handleAction(1)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleAction(state: Int) {
        connect(state)
    }

    override fun onCreate() {
        super.onCreate()
        InitMQttAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: MQService
            get() {
                Log.i(TAG, "LocalBinder")
                return this@MQService
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private fun init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            clientId = UUID.getUUID(this@MQService)
            client = MqttClient(host, clientId,
                    MemoryPersistence())
            //MQTT的连接设置
            options = MqttConnectOptions()
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options?.isCleanSession = true
            //设置连接的用户名
            options?.userName = userName
            //设置连接的密码
            options?.password = passWord.toCharArray()
            // 设置超时时间 单位为秒
            options?.connectionTimeout = 15
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options?.keepAliveInterval = 60
            options?.isAutomaticReconnect = true//打开重连机制


            //            options.setWill("sssssssss","rangossssss".getBytes("UTF-8"),1,false);

            //设置回调
            client?.setCallback(object : MqttCallback {

                override fun connectionLost(cause: Throwable) {
                    //连接丢失后，一般在这里面进行重连
                    println("connectionLost----------")
                    startReconnect()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    //publish后会执行到这里
                    println("deliveryComplete---------" + token.isComplete)
                }

                override fun messageArrived(topicName: String, message: MqttMessage) {
                    Log.i(TAG, "-->topicName:$topicName,message:$message")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun connect(state: Int) {
        try {
            ConAsync(this@MQService).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun startReconnect() {

        scheduler.scheduleAtFixedRate({
            if (!client!!.isConnected) {
                connect(1)
                Log.i(TAG, "-->startReconnect")
            }
        }, 0, 1000, TimeUnit.MILLISECONDS)
    }

    internal inner class InitMQttAsync : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            init()
            return null
        }
    }

    fun publish(topicName: String, qos: Int, payload: String): Boolean {
//        var qos = qos
        var flag = false
        try {
            if (client != null && !client!!.isConnected) {
                client!!.connect(options)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        if (client != null && client!!.isConnected) {
            try {
                val message = MqttMessage(payload.toByteArray(charset("utf-8")))
//                qos = 1
                message.qos = qos
                client!!.publish(topicName, message)
                flag = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return flag
    }

    fun subscribe(topicName: String, qos: Int): Boolean {
        var flag = false
        try {
            if (client != null && !client!!.isConnected) {
                client!!.connect(options)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        if (client != null && client!!.isConnected) {
            try {
                client!!.subscribe(topicName, qos)
                flag = true
            } catch (e: MqttException) {
                e.printStackTrace()
            }

        }
        return flag
    }

    fun unsubscribe(topicName: String) {
        if (client != null && client!!.isConnected) {
            try {
                client!!.unsubscribe(topicName)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun cancelAllsubscibe() {
        val list = topicNames
        for (i in list.indices) {
            val s = list[i]
            unsubscribe(s)
        }
    }

    inner class ConAsync(mqService: MQService) : BaseWeakAsyncTask<Int, Void, Int, MQService>(mqService) {
        override fun doInBackground(target: MQService?, vararg params: Int?): Int {
            var code=0
            try {

                if (client != null && client?.isConnected == false) {
                    client?.connect(options)
                }
                val topicNames = topicNames
                Log.i("ConAsync", "-->" + topicNames.size)
                if ((client?.isConnected==true) && !topicNames.isEmpty()) {
                    for (topicName in topicNames) {
                        if (!TextUtils.isEmpty(topicName)) {
                            client!!.subscribe(topicName, 1)
                            Log.i("client", "-->$topicName")
                        }
                    }
                    code = 100
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return code
        }


        override fun onPostExecute(mqService: MQService, code: Int?) {
            if (code == 100) {

            }
        }
    }

    internal inner class LoadDataAsync(mqService: MQService) : BaseWeakAsyncTask<List<Device>, Void, Int, MQService>(mqService) {

        override fun doInBackground(mqService: MQService, vararg lists: List<Device>): Int? {
            val devices = lists[0]
            try {
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(mqService: MQService, integer: Int?) {

        }
    }

    companion object {
        // TODO: Rename actions, choose action names that describe tasks that this
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        private val ACTION = "com.peihou.warmer.service.action"

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        fun startAction(context: Context) {
            val intent = Intent(context, MQService::class.java)
            intent.action = ACTION
            context.startService(intent)
        }
    }

}
