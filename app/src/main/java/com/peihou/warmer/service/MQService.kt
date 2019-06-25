package com.peihou.warmer.service

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.*
import android.text.TextUtils
import android.util.Log
import com.peihou.warmer.activity.*
import com.peihou.warmer.base.MyApplication
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl

import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.NetWorkUtil
import com.peihou.warmer.lib.AbsHeartBeatService
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.pojo.TimerTack
import com.peihou.warmer.receiver.MQTTMessageReveiver
import com.peihou.warmer.utils.TenTwoUtil
import com.peihou.warmer.utils.ToastUtils

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.Serializable

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
class MQService : AbsHeartBeatService() {
    override fun onStartService() {

        Log.i(TAG, "-->onStartService")
    }

    override fun onStopService() {
        Log.i(TAG, "-->onStopService")
    }

    override fun getDelayExecutedMillis(): Long {
        return 0
    }

    override fun getHeartBeatMillis(): Long {
        return 30 * 1000
    }

    override fun onHeartBeat() {
//        if (NetWorkUtil.isConn(this)) {
//            Log.i(TAG, "网络可用")
//            if (!isConnecting(client)) {
//                Log.i(TAG, "-->连接MQTT")
//                connect(1)
//            }
//            if (sendNetState){
//                sendNetState=false
//            }
//        } else {
//            if (!sendNetState){
//                sendNetState=true
//                Log.i(TAG, "网络不可用")
//            }
//        }
    }


    var sendNetState = false

    private val TAG = "MQService"
    private val host = "tcp://p99.tech"//mqtt连接服务端ip
    private val userName = "root"//mqtt连接用户名
    private val passWord = "Xr7891122"//mqtt连接密码
    //    private static final android.os.Handler mainThreadHandler = new android.os.Handler(Looper.getMainLooper());

    private var client: MqttClient? = null//mqtt客户端

    var myTopic = "rango/dc4f220aa96e/transfer"


    private var options: MqttConnectOptions? = null
    private var application: MyApplication? = null
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var clientId: String? = null
    private var binder = LocalBinder()
    private var deviceDaoImpl: DeviceDaoImpl? = null
    private val topicNames = mutableListOf<String>()
    fun getTopicNames(): List<String> {
        topicNames.clear()
        var devices = deviceDaoImpl?.findAllDevice()
        if (devices != null && !devices.isEmpty()) {
            for (device in devices) {
                var deviceMac = device.deviceMac
                var onlineTopic = "warmer/$deviceMac/transfer"
                var offlineTopic = "warmer/$deviceMac/lwt"
                var updateTopic = "warmer/$deviceMac/upgrade/transfer"
                var resetTopic = "warmer/$deviceMac/reset"
                topicNames.add(onlineTopic)
                topicNames.add(offlineTopic)
                topicNames.add(updateTopic)
                topicNames.add(resetTopic)
            }
        }
        return topicNames
    }
//        get() {
//            val topicNames = ArrayList<String>()
//            topicNames.add(myTopic)
//            return topicNames
//        }

    override fun onCreate() {
        super.onCreate()
        init()
        application = applicationContext as MyApplication?
        InitMQttAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.i(TAG, "onDestroy")
            scheduler.shutdown()
            client?.disconnect()

        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }


//    private fun listenNetworkConnectivity() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            connectivityManager?.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
//                override fun onAvailable(network: Network) {
//                    super.onAvailable(network)
//                    Log.d(TAG, "onAvailable()")
//
//                }
//
//                override fun onUnavailable() {
//                    super.onUnavailable()
//                    Log.d(TAG, "onUnavailable()")
//
//                }
//
//                override fun onLost(network: Network) {
//                    super.onLost(network)
//                    Log.d(TAG, "onLost()")
//
//                }
//            })
//        }
//    }

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

    /**
     * 初始化mqtt
     */
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
            options?.isAutomaticReconnect = false//打开重连机制
            options?.isAutomaticReconnect = true

            //            options.setWill("sssssssss","rangossssss".getBytes("UTF-8"),1,false);

            //设置回调
            client?.setCallback(object : MqttCallback {

                override fun connectionLost(cause: Throwable) {
                    //连接丢失后，一般在这里面进行重连
                    Log.i(TAG, "MQTT断开连接")
                    startReconnect()
//                    startReconnect()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    //publish后会执行到这里
                    println("deliveryComplete---------" + token.isComplete)
                }

                override fun messageArrived(topicName: String, message: MqttMessage) {
                    LoadDataAsync(this@MQService).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, topicName, message.payload)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 连接mqtt
     */
    private fun connect(state: Int) {
        try {
            ConAsync(this@MQService).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startReconnect() {
        scheduler.scheduleAtFixedRate({


            if (!isConnecting(client)) {
                Log.i(TAG, "网络可用")
                Log.i(TAG, "-->连接MQTT")
                connect(1)
            }

        }, 0, 1000, TimeUnit.MILLISECONDS)
    }

    inner class InitMQttAsync : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            deviceDaoImpl = application?.deviceDao
            return null
        }
    }

    /**
     * APP发送设备主题
     * @topicName 发送主题
     * @qos mqtt消息服务质量
     * @payLoad 发送内容
     */
    fun publish(topicName: String, qos: Int, payLoad: ByteArray): Boolean {
        var flag = false
        try {
            if (client?.isConnected == false) {
                var ss = topicName.split("/")
                var deviceMac = ss[1]
                var onlineTopic = "warmer/$deviceMac/transfer"
                var offlineTopic = "warmer/$deviceMac/lwt"
                var updateTopic = "warmer/$deviceMac/upgrade/transfer"
                var resetTopic = "warmer/$deviceMac/reset"
                subscribe(onlineTopic, 1)
                subscribe(offlineTopic, 1)
                subscribe(updateTopic, 1)
                subscribe(resetTopic, 1)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        if (client?.isConnected == true) {
            try {
                val message = MqttMessage(payLoad)
//                qos = 1
                message.qos = qos
                client?.publish(topicName, message)
                flag = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return flag
    }

    fun publish(topicName: String, qos: Int, payload: String): Boolean {
//        var qos = qos
        var flag = false
        try {
            if (client != null && client?.isConnected == false) {
                client?.connect(options)
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        if (client?.isConnected == true) {
            try {
                val message = MqttMessage(payload.toByteArray(charset("utf-8")))
//                qos = 1
                message.qos = qos
                client?.publish(topicName, message)
                flag = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return flag
    }

    /**
     * 正在连接mqtt
     */
    private fun isConnecting(client: MqttClient?): Boolean {
        var flag = false
        if (client != null) {
            flag = client.isConnected
        }
        Log.i("isConnecting", "-->$flag")
        return flag
    }

    /**
     * 订阅接收设备消息的主题
     * @topicName 接收主题
     * @qos mqtt消息服务质量
     * @return true订阅成功，false订阅失败
     */
    fun subscribe(topicName: String, qos: Int): Boolean {
        var flag = false
        try {
            if (!isConnecting(client)) {
                client?.connect(options)
            }
            if (client?.isConnected == true) {
                client?.subscribe(topicName, qos)
                flag = true
            }
        } catch (e: MqttException) {
            e.printStackTrace()
            Log.i(TAG, "-->${e.message}")
        }
        return flag
    }

    /**
     * 取消设备接收主题
     * @topicName 接收主题
     */
    fun unsubscribe(topicName: String) {
        if (client != null && client?.isConnected == true) {
            try {
                client?.unsubscribe(topicName)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 取消所有的接收主题
     */
    fun cancelAllsubscibe() {
        try {
            for(topic in topicNames){
                unsubscribe(topic)
            }
            deviceDaoImpl?.deleteAll()
        } catch (e: Exception) {
        }
    }

    /**
     * warmer/imei/set (app下发)
    warmer/imei/transfer（设备回复）
    warmer/imei/lwt(设备离线)
    warmer/imei/reset(设备重置)
    warmer/imei/upgrade/set(单播升级命令)
    warmer/imei/upgrade/transfer(单播升级回复)
    warmer/upgrade/set(广播升级命令)
    warmer/upgrade/transfer(广播升级回复)
    ps：imei = ssid + sta_mac
     */
    /**
     * 查询设备数据
     * @deviceMac 设备唯一标识
     * @code 查询设备功能码 0x11 基本功能查询 0x21 查询定时日计划 0x31查询定时周计划 0x41查询高级功能
     * @return true 发送成功 false发送失败
     */
    fun getData(deviceMac: String?, code: Int): Boolean {
        var topicName = "warmer/$deviceMac/set"
        var bytes = ByteArray(4)
        bytes[0] = 0x50
        bytes[1] = code.toByte()
        var sum = 0
        for (i in 0..1) {
            sum += bytes[i]
        }
        bytes[2] = (sum % 256).toByte()
        bytes[3] = 0x46
        return publish(topicName, 1, bytes)
    }

    /**
     * 发送基础数据
     * @deviceMac 设备mac地址
     * @code 功能码
     * @device 设备
     * @return true 发送成功 false发送失败
     */
    fun sendBasic(code: Byte, device: Device?): Boolean {
        try {
            var deviceMac = device?.deviceMac
            var topicName = "warmer/$deviceMac/set"
            var bytes = ByteArray(16)
            if (device != null) {
                bytes[0] = 0x90.toByte()
                bytes[1] = code
                bytes[2] = 0x0d
                bytes[3] = device.mode.toByte()
                bytes[4] = device.state.toByte()
                bytes[5] = device.setTemp.toByte()
                bytes[6] = device.currentTemp.toByte()
                var sum = 0
                for (i in 0..6)
                    sum += bytes[i]
                bytes[14] = (sum % 256).toByte()
                bytes[15] = 0x46
            }
            return publish(topicName, 1, bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    var dayTimerTack = mutableListOf<TimerTack>()
        get() {
            if (field.isEmpty()) {
                for (i in 0..11) {
                    field.add(TimerTack())
                }
            }
            return field
        }


    fun mDayTimerTask(): MutableList<TimerTack> {
        for (i in 0..11) {
            var timerTack = dayTimerTack[i]
            timerTack.temp = 0
            timerTack.hour = 0
            timerTack.min = 0
            dayTimerTack[i] = timerTack
        }
        return dayTimerTack
    }

    /**
     * 发送日定时
     * @deviceMac 设备状态
     * @timers 日定时列表
     * @return true 发送成功 false发送失败
     */
    fun sendDayTimer(deviceMac: String?, timers: MutableList<TimerTack>): Boolean {
        var topicName = "warmer/$deviceMac/set"
        var bytes = ByteArray(45)
        try {
            bytes[0] = 0x90.toByte()
            bytes[1] = 0x21
            bytes[2] = 0x2a
            var index = 2
            for (i in 0..11) {
                var timerTack = timers[i]
                var temp = timerTack.temp
                var hour = timerTack.hour
                var min = timerTack.min
                bytes[++index] = temp.toByte()
                bytes[++index] = hour.toByte()
                bytes[++index] = min.toByte()
            }
            var sum = 0
            for (i in 0..42) {
                sum += bytes[i]
            }
            bytes[43] = (sum % 256).toByte()
            bytes[44] = 0x46
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return publish(topicName, 1, bytes)
    }

    var weekTimerTack = mutableListOf<MutableList<TimerTack>>()
        get() {
            if (field.isEmpty()) {
                for (i in 0..6) {
                    var week = mutableListOf<TimerTack>()
                    for (j in 0..3) {
                        week.add(TimerTack())
                    }
                    field.add(week)
                }
            }
            return field
        }

    fun mGetWeekTimerTask(): MutableList<MutableList<TimerTack>> {
        for (i in 0..6) {
            for (j in 0..3) {
                var timerTack = weekTimerTack[i][j]
                timerTack.temp = 0
                timerTack.hour = 0
                timerTack.min = 0
                weekTimerTack[i][j] = timerTack
            }
        }
        return weekTimerTack
    }

    /**
     * 发送周定时
     * @deviceMac 设备地址
     * @timers 周定时列表
     * @return true 发送成功 false发送失败
     */
    fun sendWeekTimer(deviceMac: String?, timers: MutableList<MutableList<TimerTack>>): Boolean {
        var topicName = "warmer/$deviceMac/set"
        var bytes = ByteArray(93)
        try {
            bytes[0] = 0x90.toByte()
            bytes[1] = 0x31
            bytes[2] = 0x5a
            var index = 2
            for (i in 0..6) {
                var week = timers[i]
                for (j in 0..3) {
                    var timerTack = week[j]
                    var temp = timerTack.temp
                    var hour = timerTack.hour
                    var min = timerTack.min
                    bytes[++index] = temp.toByte()
                    bytes[++index] = hour.toByte()
                    bytes[++index] = min.toByte()
                }
            }
            var sum = 0
            for (i in 0..90) {
                sum += bytes[i]
            }
            bytes[91] = (sum % 256).toByte()
            bytes[92] = 0x46
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return publish(topicName, 1, bytes)
    }

    var advanceArray = IntArray(15)
    /**
     * 高级设置
     * @deviceMac 设备地址
     * @array 发送内容
     * @return true 发送成功 false发送失败
     */
    fun sendAdvanceSet(deviceMac: String?, array: IntArray): Boolean {
        var topicName = "warmer/$deviceMac/transfer"
        var bytes = ByteArray(20)
        bytes[0] = 0x90.toByte()
        bytes[1] = 0x41
        bytes[2] = 0x13
        for (i in 0..14) {
            bytes[3 + i] = array[i].toByte()
        }
        var sum = 0
        for (i in 0..17) {
            sum += bytes[i]
        }
        bytes[18] = (sum % 256).toByte()
        bytes[19] = 0x46
        return publish(topicName, 1, bytes)
    }

    /**
     * 订阅接收设备消息的主题
     */
    fun subscribeAllTopic(list: List<String>) {
        for (deviceMac in list) {
            var onlineTopic = "warmer/$deviceMac/transfer"
            var offlineTopic = "warmer/$deviceMac/lwt"
            var updateTopic = "warmer/$deviceMac/upgrade/transfer"
            var resetTopic = "warmer/$deviceMac/reset"
            subscribe(onlineTopic, 1)
            subscribe(offlineTopic, 1)
            subscribe(updateTopic, 1)
            subscribe(resetTopic, 1)
        }
    }

    /**
     * 订阅设备接收主题
     *@deviceMac 设备mac地址
     */
    fun subscribeTopic(deviceMac: String) {
        var onlineTopic = "warmer/$deviceMac/transfer"
        var offlineTopic = "warmer/$deviceMac/lwt"
        var updateTopic = "warmer/$deviceMac/upgrade/transfer"
        var resetTopic = "warmer/$deviceMac/reset"
        subscribe(onlineTopic, 1)
        subscribe(offlineTopic, 1)
        subscribe(updateTopic, 1)
        subscribe(resetTopic, 1)
    }

    inner class ConAsync(mqService: MQService) : BaseWeakAsyncTask<Int, Void, Int, MQService>(mqService) {
        override fun doInBackground(target: MQService?, vararg params: Int?): Int {
            var code = 0
            try {
                val topicNames = getTopicNames()
                for (topicName in topicNames) {
                    if (!TextUtils.isEmpty(topicName)) {
                        subscribe(topicName, 1)
                        Log.i(TAG, "-->$topicName")
                    }
                }
                code = 100

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return code
        }

        override fun onPostExecute(mqService: MQService, code: Int?) {
            if (code == 100) {
                var devices = deviceDaoImpl?.findAllDevice()
                if (devices != null && !devices.isEmpty()) {
                    LoadBasicAsync(this@MQService).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, devices)
                }
            }
        }
    }


    inner class LoadBasicAsync(mqService: MQService) : BaseWeakAsyncTask<MutableList<Device>, Unit, Unit, MQService>(mqService) {
        override fun doInBackground(target: MQService?, vararg params: MutableList<Device>) {
            var devices = params[0]
            for (device in devices) {
                var deviceMac = device.deviceMac
                getData(deviceMac, 0x11)
            }
        }

        override fun onPostExecute(target: MQService?, result: Unit?) {

        }


    }

    inner class LoadDataAsync(mqService: MQService) : BaseWeakAsyncTask<Any, Void, Int, MQService>(mqService) {
        var deviceName: String? = null;
        override fun doInBackground(target: MQService?, vararg params: Any?): Int {
            try {
                var topicName: String = params[0] as String
                var ss = topicName.split("/")
                var deviceMac = ss[1]
                var device = deviceDaoImpl?.findDevice(deviceMac)
                deviceName = device?.deviceName
                if (topicName.contains("lwt")) {
                    var intent = Intent("offline")
                    intent.putExtra("deviceMac", deviceMac)
                    intent.putExtra("device", device)
                    sendBroadcast(intent)
                    return 2
                } else if (topicName.contains("reset")) {
                    deviceDaoImpl?.delete(device)
                    if (MainActivity.running) {
                        var intent = Intent("MainActivity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("reset", 1)
                        sendBroadcast(intent)
                    } else if (DeviceActivity.running) {
                        var intent = Intent("DeviceActivity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("reset", 1)
                        sendBroadcast(intent)
                    } else if (TimerActivity.running) {
                        var intent = Intent("TimerActivity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("reset", 1)
                        sendBroadcast(intent)
                    } else if (Set01Activity.running || Set02Activity.running || Set03Activity.running || Set04Activity.running) {
                        var intent = Intent("Set01Activity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("reset", 1)
                        sendBroadcast(intent)
                    }
                    return 1
                }
                var message: ByteArray = params[1] as ByteArray
                var bytes = IntArray(message.size)
                var sum = 0
                var len = message.size
                for (i in 0 until len) {
                    bytes[i] = message[i].toInt()
                    if (bytes[i] < 0)
                        bytes[i] = bytes[i] + 256
                    if (i == len - 2) {
                        Log.i("LoadDataAsync", "check:${bytes[len - 2]}")
                    }
                    if (i < len - 2) {
                        sum += bytes[i]
                    }
                }
                var check = sum % 256
                if (check != bytes[len - 2]) {
                    Log.i("LoadDataAsync", "check:$check")
                    return 0
                }


                if (bytes[1] == 0x11 || bytes[1] == 0x61) {
                    var mode = bytes[3]
                    var state = bytes[4]
                    var setTemp = bytes[5]
                    var curTemp = bytes[6]
                    var error = bytes[7]
                    var x = TenTwoUtil.changeToTwo(state)
                    device?.mode = mode
                    device?.state = state
                    device?.setTemp = setTemp
                    device?.currentTemp = curTemp

                    if (x[0] == 1) {
                        device?.open = 0
                    } else if (x[1] == 1) {
                        device?.open = 1
                    }
                    device?.lock = x[2]
                    device?.error = error
                    device?.online = true
                    deviceDaoImpl?.update(device)
                } else if (bytes[1] == 0x21) {
                    var index = 2
                    for (i in 0..11) {
                        var timerTack = dayTimerTack[i]
                        timerTack.deviceMac = deviceMac
                        timerTack.temp = bytes[++index]
                        timerTack.hour = bytes[++index]
                        timerTack.min = bytes[++index]
                        dayTimerTack[i] = timerTack
                    }
                } else if (bytes[1] == 0x31) {
                    var index = 2
                    for (i in 0..6) {
                        for (j in 0..3) {
                            var timerTack = weekTimerTack[i][j]
                            timerTack.deviceMac = deviceMac
                            timerTack.temp = bytes[++index]
                            timerTack.hour = bytes[++index]
                            timerTack.min = bytes[++index]
                            weekTimerTack[i][j] = timerTack
                        }
                    }
                } else if (bytes[1] == 0x41) {
                    for (i in 3..17) {
                        advanceArray[i - 3] = bytes[i]
                    }
                }
                if (MainActivity.running && bytes[1] == 0x11) {
                    var intent = Intent("MainActivity")
                    intent.putExtra("deviceMac", deviceMac)
                    intent.putExtra("device", device)
                    sendBroadcast(intent)
                } else if (DeviceActivity.running && bytes[1] == 0x11) {
                    var intent = Intent("DeviceActivity")
                    intent.putExtra("deviceMac", deviceMac)
                    intent.putExtra("device", device)
                    sendBroadcast(intent)
                } else if (TimerActivity.running) {
                    if (bytes[1] == 0x21) {
                        var intent = Intent("TimerActivity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("dayTimerTack", dayTimerTack as Serializable)
                        sendBroadcast(intent)
                    } else if (bytes[1] == 0x31) {
                        var intent = Intent("TimerActivity")
                        intent.putExtra("deviceMac", deviceMac)
                        intent.putExtra("weekTimerTack", weekTimerTack as Serializable)
                        sendBroadcast(intent)
                    }
                } else if (Set01Activity.running || Set02Activity.running || Set03Activity.running || Set04Activity.running && bytes[1] == 0x41) {
                    var intent = Intent("Set01Activity")
                    intent.putExtra("deviceMac", deviceMac)
                    intent.putExtra("advanceArray", advanceArray)
                    sendBroadcast(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 0
        }


        override fun onPostExecute(mqService: MQService, integer: Int?) {
            if (!TextUtils.isEmpty(deviceName)) {
                when (integer) {
                    1 -> ToastUtils.toastShort(this@MQService, "$deviceName".plus("已重置"))
                    2 -> ToastUtils.toastShort(this@MQService, "$deviceName".plus("已离线"))
                }
            }

        }
    }


}
