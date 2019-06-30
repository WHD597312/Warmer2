package com.peihou.warmer.activity

import android.Manifest
import android.annotation.TargetApi
import android.content.*
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.OnClick
import com.amap.api.location.*
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.dialog.ChangeDialog
import com.peihou.warmer.custom.view.MyDecoration
import com.peihou.warmer.custom.view.MyHeadRefreshView
import com.peihou.warmer.custom.view.MyLoadMoreView
import com.peihou.warmer.custom.view.MyRecyclerViewItem
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl
import com.peihou.warmer.http.BaseWeakAsyncTask
import com.peihou.warmer.http.HttpUtils
import com.peihou.warmer.http.NetWorkUtil
import com.peihou.warmer.http.WeakRefHandler
import com.peihou.warmer.mvp.present.DevicePersentImpl
import com.peihou.warmer.mvp.present.IDevicePersent
import com.peihou.warmer.mvp.present.IUserPresent
import com.peihou.warmer.mvp.present.UserPresentImpl
import com.peihou.warmer.mvp.view.IUserView
import com.peihou.warmer.pojo.Device
import com.peihou.warmer.receiver.MQTTMessageReveiver
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.TenTwoUtil
import com.peihou.warmer.utils.ToastUtils
import com.peihou.warmer.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_device.view.*
import kotlinx.android.synthetic.main.item_device_weather.view.*
import org.json.JSONObject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.ArrayList

class MainActivity :BaseActivity(),EasyPermissions.PermissionCallbacks,IUserView{

    private val TAG="MainActivity"
    var params= mutableMapOf<String,Any?>()


    private var isNeedCheck = true

    var adapter:DeviceAdapter?=null
    private var userId:Int?=null
    private var devices= mutableListOf<Device>()
    private var reset=0
    override fun initParms(parms: Bundle) {
        userId=parms.getInt("userId")
        reset=parms.getInt("reset")
    }

    companion object {
        var running=false
    }
    var reeultValue=0
    override fun onStart() {
        super.onStart()
        permissionGrantedSuccess()
        if (running==false && mqService!=null && reeultValue==0){
            devices.clear()
            var devices2=deviceDaoImpl?.findAllDevice()!!
            devices.addAll(devices2)
            adapter?.notifyDataSetChanged()
            if (mqService!=null){
                if (!devices.isEmpty()){
                    countTime.start()
                    devicePresent.setDevices(this,mqService,devices)
                }
            }
        }
        running=true
    }

    override fun onStop() {
        super.onStop()
        running=false
        reeultValue=0
    }

    override fun bindLayout(): Int {
       return R.layout.activity_main
    }

    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    var userPresent:IUserPresent=UserPresentImpl(this)
    var devicePresent:IDevicePersent=DevicePersentImpl(this)
    var refresh=0
    var messageReveiver: MQTTMessageReveiver? = null

    var deviceDaoImpl:DeviceDaoImpl?=null
    override fun initView() {

        messageReveiver= MQTTMessageReveiver()
        var intentFilter=IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(messageReveiver,intentFilter)
        userPresent=UserPresentImpl(this)
        devicePresent=DevicePersentImpl(this)

        var decoration= MyDecoration()
        decoration.setDeiverHeight(getDimen(R.dimen.dp_20))
                .setColor(Color.parseColor("#f5f5f5"))
                .setMargin(0f)
        rv_device.layoutManager=LinearLayoutManager(this)
        rv_device.addItemDecoration(decoration)
        deviceDaoImpl=application.deviceDao
        devices=deviceDaoImpl?.findAllDevice()!!
        adapter=DeviceAdapter(this,devices)
        rv_device.adapter=adapter

        var intent=Intent(this,MQService::class.java)
        bind=bindService(intent,connection,Context.BIND_AUTO_CREATE)
        var filter=IntentFilter("MainActivity")
        filter.addAction("offline")
        registerReceiver(receiver,filter)
        swipeRefresh.setHeaderView(MyHeadRefreshView(this))
        swipeRefresh.setFooterView(MyLoadMoreView(this))
        if (reset==0){
            params.clear()
            params["userId"]=userId
            userPresent.operate(this,5,params)
        }
        swipeRefresh.setRefreshListener(object : BaseRefreshListener {
            override fun loadMore() {
                refresh=2
                params.clear()
                params["userId"]=userId
                userPresent.operate(this@MainActivity,5,params)

            }
            override fun refresh() {
                refresh=1
                countTime.start()
                devicePresent.setDevices(this@MainActivity,mqService,devices)
                if (!TextUtils.isEmpty(city)){
                    var url="http://apicloud.mob.com/v1/weather/query?key=2b59cd5d85164&city=$city"
                    WeatherAsync(this@MainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url)
                }

            }
        })
    }
    override fun fail(code: Int) {
        Log.i(TAG,"-->$code")
        if (code==-2){
            if (refresh==1) {
                swipeRefresh.finishRefresh()
                refresh=0
            }
            else if (refresh==2) {
                refresh=0
                swipeRefresh.finishLoadMore()
            }
            if (!devices.isEmpty()) {
                handler?.sendEmptyMessage(1001)
            }
        }else if (code==-1){
            ToastUtils.toastShort(this,"当前网络不可用")
        }else if (code==-3){
            if (refresh==1){
                refresh=0
                swipeRefresh.finishRefresh()
            }
            else if (refresh==2) {
                swipeRefresh.finishLoadMore()
                refresh=0
            }
        }else if (code==-6){
            ToastUtils.toastShort(this,"更新设备名称失败")
        }else if (code==-7){
            ToastUtils.toastShort(this,"删除设备失败")
        }

    }

    override fun success(code: Int) {
        if (code==1){
           countTime.start()
        }
        else if (code==2){
            devices.clear()
            var devices2=userPresent.getDevices()
            devices.addAll(devices2)
            adapter?.notifyDataSetChanged()
            Log.i(TAG,"-->$mqService")
            if (NetWorkUtil.isConn(this)){
                if (!devices.isEmpty()) {
                    handler?.sendEmptyMessage(1001)
                }
            }
        }else if (code==3){
            if (refresh==1){
                refresh=0
                swipeRefresh.finishRefresh()
            }
            else if (refresh==2){
                swipeRefresh.finishLoadMore()
                refresh=0
            }
        }else if (code==6){
            ToastUtils.toastShort(this,"更新设备名称成功")
            var device=devices[updatePosition]
            device.deviceName=updateDeviceName
            devices[updatePosition]=device
            adapter?.notifyDataSetChanged()
        }else if (code==7){
            ToastUtils.toastShort(this,"删除设备成功")
            devices.removeAt(updatePosition)
            adapter?.notifyDataSetChanged()
        }
    }

    private var handler:Handler?=null
    get() {
        if (field==null){
            field=WeakRefHandler(mCallback)
        }
        return field
    }
    private var mCallback: Handler.Callback = Handler.Callback { msg ->
        val what = msg.what
        if (what==1001){
            countTime.start()
            devicePresent.setDevices(this,mqService,devices)
        }
        true
    }

    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#ffffff")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bind){
            unbindService(connection)
        }
        unregisterReceiver(receiver)
        unregisterReceiver(messageReveiver)
    }
    override fun onBackPressed() {
        application.removeAllActivity()
    }
    @OnClick(R.id.img_header,R.id.img_add)
    fun onClick(view:View){
        when(view.id){
            R.id.img_header->startActivity(PersonSetActivity::class.java)
            R.id.img_add->{
                var intent=Intent(this,AddDeviceActivity::class.java)
                intent.putExtra("userId",userId)
                startActivityForResult(intent,100)
            }
        }
    }
    @TargetApi(23)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 把执行结果的操作给EasyPermissions
        println(requestCode)
        if (isNeedCheck) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
        }
    }
    override fun onPermissionsDenied(requestCode: Int, perms:List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)) {
            AppSettingsDialog.Builder(this)
                    .setTitle("提示")
                    .setRationale("请点击\"设置\"打开定位权限。")
                    .setPositiveButton("设置")
                    .setNegativeButton("取消")
                    .build()
                    .show()
            isNeedCheck = false
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {

    }

    @AfterPermissionGranted(0)
    private fun permissionGrantedSuccess() {
        val perms = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            initLocation()
            startLocation()//开始定位
            // 已经申请过权限，做想做的事
        } else {

            if (isNeedCheck) {
                EasyPermissions.requestPermissions(this, getString(R.string.location),
                        0, *perms)

            }
        }
    }
    inner class DeviceAdapter(private var context: Context,private var devices:MutableList<Device>):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType==0){
                var view:View=View.inflate(context,R.layout.item_device_weather,null)
                return WeatherHolder(view)
            }else{
                var itemView:View=View.inflate(context,R.layout.item_device,null)
                return DeviceHolder(itemView)
            }
        }

        override fun getItemCount(): Int {

            return devices.size+1
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
           Log.i("ViewHolder","-->${position}")
            if (position==0){
                var weatherHolder:WeatherHolder=viewHolder as WeatherHolder
                weatherHolder.bind(temperature,humidity,weather)
            } else if (position>0){
                var deviceHolder:DeviceHolder= viewHolder as DeviceHolder
                var device=devices[position-1]
                deviceHolder.bind(device)
                deviceHolder.scroll_item.reset()
                deviceHolder.btn_editor.setOnClickListener {
                    changeDialog(0,position-1,device.deviceId)
                }
                deviceHolder.btn_delete.setOnClickListener {
                    changeDialog(1,position-1,device.deviceId)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position==0){
                return 0
            }else
                return 1
        }
    }
    inner class WeatherHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var tv_temp:TextView?=null
        var tv_hum:TextView?=null
        var img_weather:ImageView?=null
        init {
            tv_temp=itemView.tv_temp
            tv_hum=itemView.tv_hum
            img_weather=itemView.img_weather
        }

        fun bind(temp:String,hum:String,weather:String){
            tv_temp?.text=temp
            tv_hum?.text=hum
            if (weather.contains("晴")){
                img_weather?.setImageResource(R.mipmap.img_sun)
            }else if (weather.contains("云")){
                img_weather?.setImageResource(R.mipmap.img_sun_cloud)
            }else if (weather.contains("雨")){
                img_weather?.setImageResource(R.mipmap.img_rain)
            }else if (weather.contains("雪")){
                img_weather?.setImageResource(R.mipmap.img_snow)
            }
        }
    }

    inner class DeviceHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var btn_editor:Button
        var btn_delete:Button
        var scroll_item:MyRecyclerViewItem
        var tv_device_name:TextView
        var image_device:ImageView
        private var image_switch:ImageView
        var tv_device_temp:TextView
        var layout_item:RelativeLayout
        init {
            btn_editor=itemView.btn_editor
            btn_delete=itemView.btn_delete
            scroll_item=itemView.scroll_item
            tv_device_name=itemView.tv_device_name
            image_device=itemView.image_device
            image_switch=itemView.image_switch
            tv_device_temp=itemView.tv_device_temp
            layout_item=itemView.layout_item
        }
        fun bind(device: Device){
            tv_device_name.text=device.deviceName
            if (device.online){
                image_device.setImageResource(R.mipmap.img_device_online)
                if (device.open==1)
                    image_switch.setImageResource(R.mipmap.img_open)
                else
                   image_switch.setImageResource(R.mipmap.img_close)
                tv_device_temp.text="${device.currentTemp}℃"
            }else{
                image_device.setImageResource(R.mipmap.img_device_offline)
                image_switch.setImageResource(R.mipmap.img_close)
            }
            image_switch.setOnClickListener {
                if (isDialogShow){
                    ToastUtils.toastShort(this@MainActivity,"请稍后...")
                    return@setOnClickListener
                }
                if (device.online){
                    var x=TenTwoUtil.changeToTwo(device.state)
                    if (device.open==1) {
                        x[0] = 1
                        x[1]=0
                    }
                    else{
                        x[0]=0
                        x[1]=1
                    }
                    device.state=TenTwoUtil.changeToTen2(x)
                    devicePresent.sendBasic(mqService,device,0x11)
                }else{
                    ToastUtils.toastShort(this@MainActivity,"该设备已离线")
                    devicePresent.getData(mqService,device.deviceMac,0x11)
                }
            }
            layout_item.setOnClickListener {
                if (isDialogShow){
                    return@setOnClickListener
                }
                if (device.online){
                    var intent=Intent(this@MainActivity,DeviceActivity::class.java)
                    intent.putExtra("device",device)
                    startActivityForResult(intent,1000)
                }else{
                    ToastUtils.toastShort(this@MainActivity,"设备已离线")
                    devicePresent.getData(mqService,device.deviceMac,0x11)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==100){
            reeultValue=1
            params.clear()
            params["userId"]=userId
            userPresent.operate(this@MainActivity,5,params)
        }else if (resultCode==1000){
            reeultValue=1
            devices.clear()
            var devices2=deviceDaoImpl?.findAllDevice()!!
            devices.addAll(devices2)
            adapter?.notifyDataSetChanged()
        }
    }
    var changeDialog: ChangeDialog?=null
    var updatePosition=-1
    var updateDeviceName:String?=null
    fun changeDialog(code:Int,position:Int,deviceId: Int){
        if (changeDialog!=null && changeDialog?.isShowing==true){
            return
        }
        changeDialog= ChangeDialog(this)
        changeDialog?.setCanceledOnTouchOutside(false)
        if (code==1){
            changeDialog?.mode=1
            changeDialog?.title="删除设备"
            changeDialog?.tips="确定删除设备?"
        }else if (code==0){
            changeDialog?.mode=0
            changeDialog?.title="编辑"
            changeDialog?.tips="编辑设备名称"
        }
        changeDialog?.show()
        changeDialog?.setOnNegativeClickListener {
            changeDialog?.dismiss()
        }

        changeDialog?.setOnPositiveClickListener {
            changeDialog?.dismiss()
            if (code==1){
                updatePosition=position
                updatePosition=position
                params.clear()
                params["deviceId"]=deviceId
                params["status"]=1
                userPresent.operate(this,7,params)
            }else{
                var content=changeDialog?.content
                updateDeviceName=content
                if (!TextUtils.isEmpty(content)){
                    updatePosition=position
                    params.clear()
                    params["deviceId"]=deviceId
                    params["deviceName"]=content
                    userPresent.operate(this,6,params)
                }else{
                    ToastUtils.toastShort(this,"设备名称不能为空")
                }
            }
        }
        backgroundAlpha(0.6f)
        changeDialog?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }
    }


    var mqService:MQService?=null
    var bind=false

    private var connection=object :ServiceConnection{
        override fun onServiceDisconnected(p0: ComponentName?) {

        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
             var binder:MQService.LocalBinder = p1 as MQService.LocalBinder
            mqService=binder.service
        }
    }

    private var receiver= object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if ("offline"==intent.action){
                Log.i("MainReceiver","-->OFFLINE")
                var len=devices.size
                for (i in 0 until len){
                    var device=devices[i]
                    device.online=false
                    devices[i]=device
                }
                adapter?.notifyDataSetChanged()
            }else{
                var deviceMac=intent.getStringExtra("deviceMac")
                var reset=intent.getIntExtra("reset",0)
                if (reset==1){
                    var len=devices.size
                    var index=-1
                    for (i in 0 until len){
                        var device=devices[i]
                        if (deviceMac==device.deviceMac){
                            index=i
                            break
                        }
                    }
                    if (index!=-1){
                        devices.removeAt(index)
                        adapter?.notifyDataSetChanged()
                    }
                }else{
                    var device2=intent.getSerializableExtra("device") as Device
                    var len=devices.size
                    for (i in 0 until len){
                        var device=devices[i]
                        if (deviceMac==device.deviceMac){
                            devices[i]=device2
                            adapter?.notifyDataSetChanged()
                            break
                        }
                    }
                }

            }
        }
    }
    private var locationClient: AMapLocationClient? = null
    private var locationOption: AMapLocationClientOption? = null
    private var province:String?=null
    private var city:String=""
    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun initLocation() {
        //初始化client
        locationClient = AMapLocationClient(applicationContext)
        locationOption = getDefaultOption()
        //设置定位参数
        locationClient?.setLocationOption(locationOption)
        // 设置定位监听
        locationClient?.setLocationListener(locationListener)
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun getDefaultOption(): AMapLocationClientOption {
        val mOption = AMapLocationClientOption()
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.isGpsFirst = false//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.httpTimeOut = 30000//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.interval = 2000//可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true//可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocation = false//可选，设置是否单次定位。默认是false
        mOption.isOnceLocationLatest = false//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP)//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.isSensorEnable = false//可选，设置是否使用传感器。默认是false
        mOption.isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true
        return mOption
    }

    /**
     * 定位监听
     */
    internal var locationListener: AMapLocationListener = AMapLocationListener { location ->
        if (null != location) {

            val sb = StringBuffer()
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (location.errorCode == 0) {
                sb.append("定位成功" + "\n")
                sb.append("定位类型: " + location.locationType + "\n")
                sb.append("经    度    : " + location.longitude + "\n")
                sb.append("纬    度    : " + location.latitude + "\n")
                sb.append("精    度    : " + location.accuracy + "米" + "\n")
                sb.append("提供者    : " + location.provider + "\n")

                sb.append("速    度    : " + location.speed + "米/秒" + "\n")
                sb.append("角    度    : " + location.bearing + "\n")
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : " + location.satellites + "\n")
                sb.append("国    家    : " + location.country + "\n")
                sb.append("省            : " + location.province + "\n")
                sb.append("市            : " + location.city + "\n")
                sb.append("城市编码 : " + location.cityCode + "\n")

                sb.append("区            : " + location.district + "\n")
                sb.append("区域 码   : " + location.adCode + "\n")
                sb.append("地    址    : " + location.address + "\n")
                sb.append("兴趣点    : " + location.poiName + "\n")
                //定位完成的时间
                sb.append("定位时间: " + Utils.formatUTC(location.time, "yyyy-MM-dd HH:mm:ss") + "\n")
            } else {
                //定位失败
                sb.append("定位失败" + "\n")
                sb.append("错误码:" + location.errorCode + "\n")
                sb.append("错误信息:" + location.errorInfo + "\n")
                sb.append("错误描述:" + location.locationDetail + "\n")
            }
            sb.append("***定位质量报告***").append("\n")
            sb.append("* WIFI开关：").append(if (location.locationQualityReport.isWifiAble) "开启" else "关闭").append("\n")
            sb.append("* GPS状态：").append(getGPSStatusString(location.locationQualityReport.gpsStatus)).append("\n")
            sb.append("* GPS星数：").append(location.locationQualityReport.gpsSatellites).append("\n")
            sb.append("****************").append("\n")
            //定位之后的回调时间
            sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n")

            //解析定位结果，
            val result = sb.toString()
            Log.i("reSult", "-->$result")

            if ("定位失败" == result) {
            }
            province = location.province
            city = location.city

            if (!TextUtils.isEmpty(city)) {
                stopLocation()
                destroyLocation()
                if (city.contains("市")){
                    var len=city.length-1
                    city=city.substring(0,len)
                    var url="http://apicloud.mob.com/v1/weather/query?key=2b59cd5d85164&city=$city"
                    WeatherAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url)
                }
            }
        }
    }
    var temperature:String="0℃"
    var humidity:String="0%"
    var weather:String="晴"
    inner class WeatherAsync(activity: MainActivity):BaseWeakAsyncTask<String,Void,Int,MainActivity>(activity){
        override fun doInBackground(target: MainActivity?, vararg params: String?): Int {
            var code=0
            var url=params[0]
            var result=HttpUtils.requestGet(url,1)
            try {
                if (!TextUtils.isEmpty(result)){
                    Log.i("WeatherAsync","-->$result")
                    var jsonObject=JSONObject(result)
                    if (jsonObject.getInt("retCode")==200){
                        var jsonArray=jsonObject.getJSONArray("result")
                        var jsonObject2=jsonArray.getJSONObject(0)
                        temperature=jsonObject2.getString("temperature")
                        var humidity2=jsonObject2.getString("humidity")
                        humidity=humidity2.substring(humidity2.indexOf("：")+1)
                        weather=jsonObject2.getString("weather")
                        code=200
                    }
                }
            } catch (e: Exception) {
            }
            return code
        }

        override fun onPostExecute(target: MainActivity?, result: Int?) {
            if (result==200 && adapter!=null){
                adapter?.notifyDataSetChanged()
            }
        }

    }

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */
    private fun getGPSStatusString(statusCode: Int): String {
        var str = ""
        when (statusCode) {
            AMapLocationQualityReport.GPS_STATUS_OK -> str = "GPS状态正常"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER -> str = "手机中没有GPS Provider，无法进行GPS定位"
            AMapLocationQualityReport.GPS_STATUS_OFF -> str = "GPS关闭，建议开启GPS，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING -> str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION -> str = "没有GPS定位权限，建议开启gps定位权限"
        }
        return str
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun startLocation() {
        //根据控件的选择，重新设置定位参数
        //        resetOption();
        // 设置定位参数
        locationClient?.setLocationOption(locationOption)
        // 启动定位
        locationClient?.startLocation()
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun stopLocation() {
        // 停止定位
        locationClient?.stopLocation()
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient?.onDestroy()
            locationClient = null
            locationOption = null
        }
    }

}