package com.peihou.warmer.activity


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.OnClick
import com.peihou.warmer.R
import com.peihou.warmer.base.BaseActivity
import com.peihou.warmer.custom.dialog.ChangeDialog
import com.peihou.warmer.custom.dialog.DownloadDialog
import com.peihou.warmer.custom.view.MyDecoration
import com.peihou.warmer.http.WeakRefHandler
import com.peihou.warmer.service.MQService
import com.peihou.warmer.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_person_set.*
import kotlinx.android.synthetic.main.dialog_down.*
import kotlinx.android.synthetic.main.item_person.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.math.BigDecimal
import java.util.*

class PersonSetActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private var isNeedCheck = true
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                    .setTitle("提示")
                    .setRationale("请点击\"设置\"打开存储权限。")
                    .setPositiveButton("设置")
                    .setNegativeButton("取消")
                    .build()
                    .show()
            isNeedCheck = false
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {

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

    var flag = 0
    @AfterPermissionGranted(0)
    private fun permissionGrantedSuccess() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            flag = 1
            Log.i("GrantedSuccess", "--flag=$flag")
        } else {

            if (isNeedCheck) {
                EasyPermissions.requestPermissions(this, getString(R.string.save),
                        0, *perms)

            }
        }
    }

    private var userPreferences: SharedPreferences? = null
    private var list = mutableListOf<String>()
    var adapter: MyAdapter? = null
    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout(): Int {
        return R.layout.activity_person_set
    }

    override fun onResume() {
        super.onResume()
        permissionGrantedSuccess()
    }

    private fun getDimen(dimenId: Int): Float {
        return resources.getDimension(dimenId)
    }

    var bind = false
    override fun initView() {
        userPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        var userName = userPreferences?.getString("userName", "")
        tv_phone.text = userName
//        list.add("常见问题")
        list.add("检查最新版本")
        list.add("清除缓存")
        list.add("更改密码")

        var service = Intent(this, MQService::class.java)
        bind = bindService(service, connect, Context.BIND_AUTO_CREATE)
        var deration = MyDecoration()
        deration.setDeiverHeight(getDimen(R.dimen.dp_1))
                .setMargin(getDimen(R.dimen.dp_30))
                .setColor(Color.parseColor("#d8dbd5"))
        rl_view.addItemDecoration(deration)
        rl_view.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(this, list)
        rl_view.adapter = adapter
    }

    @OnClick(R.id.img_back, R.id.btn_exit)
    fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> finish()
            R.id.btn_exit -> exit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bind)
            unbindService(connect)
    }

    override fun setStatusColor(color: Int): Int {
        return Color.parseColor("#ffffff")
    }

    var changeDialog: ChangeDialog? = null
    fun changeDialog(code: Int) {
        if (changeDialog != null && changeDialog?.isShowing == true) {
            return
        }
        changeDialog = ChangeDialog(this)
        changeDialog?.setCanceledOnTouchOutside(false)
        if (code == 1) {
            changeDialog?.mode = 1
            changeDialog?.title = "清除缓存"
            changeDialog?.tips = "确定清除缓存?"
        } else if (code == 2) {
            changeDialog?.mode = 1
            changeDialog?.title = "版本更新"
            changeDialog?.tips = "检查最新版本!"
        }
        changeDialog?.show()
        changeDialog?.setOnNegativeClickListener {
            backgroundAlpha(1.0f)
            changeDialog?.dismiss()
        }
        changeDialog?.setOnPositiveClickListener {
            if (code==2){
                ToastUtils.toastShort(this,"已是最新版本")
            }else if (code==1){
                ToastUtils.toastShort(this,"缓存已清除!")
            }
            backgroundAlpha(1.0f)
            changeDialog?.dismiss()


        }
        backgroundAlpha(0.6f)
        changeDialog?.setOnDismissListener {
            if (code==1) {
                backgroundAlpha(1.0f)
            }
        }
    }


    internal var dialog: DownloadDialog? = null
    var downloadUrl = "https://o.app.qq.com/image/microQr.png?template=1&pkgName=com.peihou.willgood2"
    private var timer: Timer? = null
    var id: Long = 0
    private fun downDialog() {
        if (dialog != null && dialog?.isShowing == true) {
            return
        }
        dialog = DownloadDialog(this)
        dialog?.setCanceledOnTouchOutside(false)

        dialog?.show()
        dialog?.setOnNegativeClickListener {
            if (downing==1) {
                downing=0
                timer?.cancel()
            }
            dialog?.dismiss()
        }

        backgroundAlpha(0.6f)
        dialog?.setOnDismissListener {
            backgroundAlpha(1.0f)
        }
    }

    var downing=0
    private fun downLoadApp() {
        downDialog()
        var downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request = DownloadManager.Request(Uri.parse(downloadUrl))

        request.setTitle("取暖器")
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setMimeType("application/vnd.android.package-archive")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir()
        //设置文件存放路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "warmer.apk")
        val query = DownloadManager.Query()

        timer = Timer()
        task = object : TimerTask() {
            override fun run() {
                val cursor = downloadManager.query(query.setFilterById(id))
                if (cursor != null && cursor.moveToFirst()) {
                    if (cursor.getInt(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                       dialog?.progress=100
                        install(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/warmer.apk")
                        task?.cancel()
                    }
                    val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
//                    val address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    Log.i("bytes_downloaded", "-->$bytes_downloaded")
                    Log.i("bytes_total", "-->$bytes_total")
                    val x = bytes_downloaded.toFloat()
                    val ss = x / bytes_total * 100
                    val bigDecimal = BigDecimal(ss.toDouble())
                    val bigDecimal2 = bigDecimal.setScale(0, BigDecimal.ROUND_DOWN)
                    val pro = Integer.parseInt("".plus(bigDecimal2))
                    Log.i("prossssssssssssss", "-->" + bytes_downloaded / bytes_total)

                    val msg = handler?.obtainMessage()
                    val bundle = Bundle()
                    bundle.putInt("pro", pro)
                    bundle.putString("name", title)
                    if (msg!=null){
                        msg.data = bundle
                        handler?.sendMessage(msg)
                    }

                }
                cursor.close()
            }
        }
        timer?.schedule(task, 0, 1000)
        dialog?.setOnPositiveClickListener {

            if (downing==1) {
                ToastUtils.toastShort(this,"当前下载任务正在进行")
                return@setOnPositiveClickListener
            }
            var path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/warmer.apk"
            var file=File(path)
            if (file.exists()){
                Log.i("downLoadApp","-->下载app文件存在")
                file.delete()
            }else{
                Log.i("downLoadApp","-->下载app文件不存在")
            }
            downing=1
            id = downloadManager.enqueue(request)
            task?.run()
        }
    }
   var handler:Handler?=null
       get() {
           if (field==null){
               field=WeakRefHandler(mCallback)
           }
           return field
       }
    private var TAG = "DownProgress"
    private var mCallback: Handler.Callback = Handler.Callback { msg ->
        val bundle = msg.data
        val pro = bundle.getInt("pro")
        Log.i("downLoadApp","-->下载进度pro=$pro")
        dialog?.progress=pro
        if (pro == 100 && dialog != null && dialog?.isShowing==true) {
            dialog?.dismiss()
        }
        true
    }

//    var handler: Handler = object : Handler() {
//        override fun handleMessage(msg: Message) {
//            super.handleMessage(msg)
//            val bundle = msg.data
//            val pro = bundle.getInt("pro")
//            Log.i("downLoadApp","-->下载进度pro=$pro")
//            dialog?.progress=pro
//            if (pro == 100 && dialog != null && dialog?.isShowing == true) {
//                dialog?.dismiss()
//            }
//        }
//    }

    private fun install(path: String) {
        var intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags=Intent.FLAG_GRANT_READ_URI_PERMISSION
            var contentUri = FileProvider.getUriForFile(this, "com.peihou.warmer.fileprovider", File(path))
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(File(path)), "application/vnd.android.package-archive")
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
    internal var task: TimerTask? = null
    /**
     * 退出
     */
    fun exit() {
        if (mqService != null) {
            mqService?.cancelAllsubscibe()
        }
        var intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("exit", 1)
        var editor = userPreferences?.edit()
        editor?.remove("userPassword")?.commit()
        startActivity(intent)
        application.removeAllActivity()
    }

    inner class MyAdapter(private var context: Context, private var list: List<String>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            var view: View = View.inflate(context, R.layout.item_person, null)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, poition: Int) {
            var content = list[poition]
            holder.tv_name?.text = content

            holder.rl_layout?.setOnClickListener {
                if (poition == 0) {
                    changeDialog(2)
                } else if (poition == 1) {
                    changeDialog(1)
                } else if (poition == 2) {
                    var intent = Intent(context, RegisterActivity::class.java)
                    intent.putExtra("code", 3)
                    intent.putExtra("phone", tv_phone.text.toString())
                    startActivityForResult(intent, 101)
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_name: TextView? = null
        var rl_layout: RelativeLayout? = null

        init {
            tv_name = itemView.tv_name
            rl_layout = itemView.rl_layout
        }
    }

    var mqService: MQService? = null
    private var connect = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            var binder: MQService.LocalBinder = service as MQService.LocalBinder
            mqService = binder.service
        }

    }
}
