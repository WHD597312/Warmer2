package com.peihou.warmer.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.peihou.warmer.R;
import com.peihou.warmer.base.BaseActivity;
import com.peihou.warmer.custom.dialog.DialogLoad;
import com.peihou.warmer.esptouch.EspWifiAdminSimple;
import com.peihou.warmer.esptouch.EsptouchTask;
import com.peihou.warmer.esptouch.IEsptouchListener;
import com.peihou.warmer.esptouch.IEsptouchResult;
import com.peihou.warmer.esptouch.IEsptouchTask;
import com.peihou.warmer.esptouch.task.__IEsptouchTask;
import com.peihou.warmer.http.BaseWeakAsyncTask;
import com.peihou.warmer.http.HttpUtils;
import com.peihou.warmer.http.NetWorkUtil;
import com.peihou.warmer.service.MQService;
import com.peihou.warmer.utils.IsChinese;
import com.peihou.warmer.utils.ToastUtils;
import com.wang.avi.AVLoadingIndicatorView;
import com.winnermicro.smartconfig.ConfigType;
import com.winnermicro.smartconfig.IOneShotConfig;
import com.winnermicro.smartconfig.SmartConfigFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddDeviceActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.btn_match)
    Button btn_match;
    @BindView(R.id.img_back)
    ImageView img_back;
//    GifDrawable gifDrawable;


    @BindView(R.id.tv_wifi)
    TextView et_ssid;
    @BindView(R.id.et_pswd)
    EditText et_pswd;
    @BindView(R.id.rl_bottom)
    RelativeLayout rl_bottom;
    private int match = 0;

    private String wifiName;
    private String wifiPswd;
    private boolean isMatching = false;

    @Override
    protected int setStatusColor(int color) {
        return Color.parseColor("#00ffffff");
    }

    @OnClick({R.id.img_back, R.id.btn_match})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                match = 0;
                Log.i("dialog", "sssssss");
                et_ssid.setEnabled(true);
                et_pswd.setEnabled(true);
                btn_match.setEnabled(true);
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }

                if (popupWindow2 != null && popupWindow2.isShowing()) {
                    isMatching = false;
                    wifiName = "";
//                    if (gifDrawable != null && gifDrawable.isRunning()) {
//                        gifDrawable.stop();
//                    }
                    avi.hide();
                    popupWindow2.dismiss();
//                    backgroundAlpha(1f);
                    break;
                }
                finish();
                break;
            case R.id.btn_match:
                try {
                    boolean conn = NetWorkUtil.isConn(AddDeviceActivity.this);
                    if (conn) {
                        String ssid = et_ssid.getText().toString();
                        String apPassword = et_pswd.getText().toString();
                        String apBssid = bSsid;
                        String taskResultCountStr = "1";
                        if (__IEsptouchTask.DEBUG) {

                        }
                        if (TextUtils.isEmpty(ssid)) {
                            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "请连接英文名称的WiFi");
                            break;
                        }
                        if (TextUtils.isEmpty(apPassword)) {
                            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "请输入wifi密码");
                            break;
                        }

//                    popupWindow();
                        match = 1;
                        et_ssid.setEnabled(false);
                        et_pswd.setEnabled(false);
                        btn_match.setEnabled(false);
//                            setLoadDialog2();
                        wifiName = ssid;
                        wifiPswd = apPassword;
                        isMatching = true;

                        isThreadDisable = false;
                        udpHelperAsync = new UDPHelperAsync();
                        udpHelperAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        congigAsync = new CongigAsync();
                        congigAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wifiName, wifiPswd);

//                            new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);

                    } else {
                        ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "请检查网络");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }


    boolean isThreadDisable = false;
    UDPHelperAsync udpHelperAsync;
    DatagramSocket datagramSocket;
    String deviceMac = "";

    class UDPHelperAsync extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int code = 0;
            Integer port = 65534;
            // 接收的字节大小，客户端发送的数据不能超过这个大小
            byte[] message = new byte[100];
            try {
                // 建立Socket连接
                datagramSocket = new DatagramSocket(port);
                datagramSocket.setBroadcast(true);
                datagramSocket.setSoTimeout(1000 * 60);
                DatagramPacket datagramPacket = new DatagramPacket(message,
                        message.length);
                try {
                    while (!isThreadDisable) {
                        // 准备接收数据
                        Log.i("UDPHelperAsync", "准备接受");
                        try {
                            datagramSocket.receive(datagramPacket);
                            String strMsg = new String(datagramPacket.getData(), "UTF-8");
//                            int count = datagramPacket.getLength();
//                            for(int i=0;i<count;i++){
//                                strMsg += String.format("%02x", datagramPacket.getData()[i]);
//                            }
                            deviceMac = wifiName + strMsg;
//                            strMsg = strMsg.toUpperCase() + ";" + datagramPacket.getAddress().getHostAddress().toString();

                            Log.i("UDPHelperAsync", datagramPacket.getAddress()
                                    .getHostAddress().toString()
                                    + ":" + strMsg);
                            code = 100;
                        } catch (Exception ex) {
                            Log.i("UDPHelperAsync", "UDP Receive Timeout.");
                            code = -1;
                            return -1;
                        }
                    }
                } catch (Exception e) {//IOException
                    e.printStackTrace();
                } finally {
                    Log.i("UDPHelperAsync", "释放资源");
                    datagramSocket.close();
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code == -1) {
                datagramSocket = null;
                if (popupWindow2 != null && popupWindow2.isShowing()) {
                    popupWindow2.dismiss();
                }
                ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "配置失败");
            } else if (code == 100) {
//                ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this,deviceMac);
                params.put("deviceMac", deviceMac);
                params.put("userId", userId);
                new AddDeviceAsync(AddDeviceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        }
    }

    CongigAsync congigAsync;

    class CongigAsync extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            popupmenuWindow3();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int code = 0;
            String ssid = strings[0];
            String pswd = strings[1];
            Log.i("ssid","-->"+ssid+","+pswd);
            oneshotConfig.start(ssid, pswd, 60, AddDeviceActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }

    class AddDeviceAsync extends BaseWeakAsyncTask<Map<String, Object>, Void, Integer, AddDeviceActivity> {

        public AddDeviceAsync(AddDeviceActivity addDeviceActivity) {
            super(addDeviceActivity);
        }

        @Override
        protected Integer doInBackground(AddDeviceActivity addDeviceActivity, Map<String, Object>... maps) {
            int returnCode = 0;
            String url = HttpUtils.ipAddress + "device/insertDevice";
            Map<String, Object> params = maps[0];
            String result = HttpUtils.requestPost(url, params);
            try {
                if (!TextUtils.isEmpty(result)) {
                    JSONObject jsonObject = new JSONObject(result);
                    returnCode = jsonObject.getInt("returnCode");
                    SharedPreferences.Editor editor = wifi.edit();
                    editor.putString(wifiName,wifiPswd);
                    editor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returnCode;
        }

        @Override
        protected void onPostExecute(AddDeviceActivity addDeviceActivity, Integer returnCode) {
            if (popupWindow2 != null && popupWindow2.isShowing()) {
                popupWindow2.dismiss();
            }
            if (returnCode == 200) {
                ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "添加设备成功");
                setResult(100);
                finish();
            } else {
                ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "添加失败,请重置设备重新添加");
            }
        }
    }

    int userId;
    private Map<String, Object> params = new HashMap<>();

    @Override
    public void initParms(Bundle parms) {
        userId = parms.getInt("userId");
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device;
    }

    SharedPreferences wifi;
    private SmartConfigFactory factory = null;
    private IOneShotConfig oneshotConfig = null;

    @Override
    public void initView() {
        wifi = getSharedPreferences("wifi", MODE_PRIVATE);
        mWifiAdmin = new EspWifiAdminSimple(this);
        et_ssid.setFocusable(false);
        registerBroadcastReceiver();
        Intent service = new Intent(AddDeviceActivity.this, MQService.class);
        bound = bindService(service, connection, Context.BIND_AUTO_CREATE);
        factory = new SmartConfigFactory();
        //通过修改参数ConfigType，确定使用何种方式进行一键配置，需要和固件侧保持一致。
        oneshotConfig = factory.createOneShotConfig(ConfigType.UDP);
    }


    private static final String TAG = "Esptouch";
    private EspWifiAdminSimple mWifiAdmin;


    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
//                Toast.makeText(AddDeviceActivity.this, text,
//                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private int type;
    int count = 0;

    MQService mqService;
    private boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQService.LocalBinder binder = (MQService.LocalBinder) service;
            mqService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    String macAddress;
    private IEsptouchTask mEsptouchTask;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
//            CountTimer countTimer = new CountTimer(30000, 1000);
//            countTimer.start();
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE

                String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, AddDeviceActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
//                    .setEnabled(true);
//            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
//                    "确认");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    String ssid = "";
                    try {
                        String deviceMac = "";
                        Log.i("diff", "-->" + System.currentTimeMillis());
                        for (IEsptouchResult resultInList : result) {
                            ssid = resultInList.getBssid();
                            Log.i("ssidssid", "-->" + ssid);
                            sb.append("配置成功");
                            String wifiName = et_ssid.getText().toString();
                            deviceMac = wifiName + ssid;

                            if (!TextUtils.isEmpty(deviceMac)) {
                                dialogLoad.dismiss();
                                params.put("deviceMac", deviceMac);
                                params.put("userId", userId);
//                                new AddDeviceAsync(AddDeviceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                 ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this,deviceMac);
//                                 SharedPreferences.Editor editor=wifi.edit();
//                                 String wifiPassword = et_pswd.getText().toString();
//                                 editor.putString(wifiName,wifiPassword);
//                                 editor.commit();
//                                 Intent intent=new Intent();
//                                 intent.putExtra("deviceMac",deviceMac);
//                                 setResult(100,intent);
//                                 finish();
                                break;
                            }
                            count++;
                            if (count >= maxDisplayCount) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                } else {
                    if (popupWindow2 != null && popupWindow2.isShowing()) {
                        isMatching = false;
                        wifiName = "";


                        if (et_ssid != null) {
                            et_ssid.setEnabled(true);
                        }
                        if (et_pswd != null) {
                            et_pswd.setEnabled(true);
                        }
                        if (btn_match != null) {
                            btn_match.setEnabled(true);
                            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "配置失败");
                        }

                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }

                        match = 0;
                        popupWindow2.dismiss();
                    }
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            et_ssid.setEnabled(true);
            et_pswd.setEnabled(true);
            btn_match.setEnabled(true);
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
            match = 0;
            if (popupWindow2 != null && popupWindow2.isShowing()) {
                avi.hide();
                isMatching = false;
                wifiName = "";
                popupWindow2.dismiss();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private PopupWindow popupWindow2;
    AVLoadingIndicatorView avi;

    public void popupmenuWindow3() {
        if (popupWindow2 != null && popupWindow2.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_help2, null);
        avi = view.findViewById(R.id.avi);
        avi.show();

        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        //添加弹出、弹入的动画
        popupWindow2.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.6f);
        popupWindow2.setFocusable(false);
        popupWindow2.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow2.showAsDropDown(rl_bottom, 0, 0);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        popupWindow2.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!isThreadDisable && datagramSocket != null) {
                    datagramSocket.close();
                }
                isThreadDisable = true;
                if (udpHelperAsync != null) {
                    udpHelperAsync.cancel(true);
                }
                if (congigAsync != null) {
                    congigAsync.cancel(true);
                }
                udpHelperAsync = null;
                congigAsync = null;
                oneshotConfig.stop();
                et_ssid.setEnabled(true);
                et_pswd.setEnabled(true);
                btn_match.setEnabled(true);
                backgroundAlpha(1.0f);
            }
        });
        //添加按键事件监听
    }


// PopupWindow popupWindow2;
//    GifImageView image_heater_help;
//    public void popupmenuWindow3() {
//        if (popupWindow2 != null && popupWindow2.isShowing()) {
//            return;
//        }
//        View view = View.inflate(this, R.layout.popup_help2, null);
//        image_heater_help = (GifImageView) view.findViewById(R.id.image_heater_help);
//        try {
//            gifDrawable = new GifDrawable(getResources(), R.mipmap.touxiang3);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        image_heater_help.setVisibility(View.VISIBLE);
//        if (gifDrawable != null) {
//            gifDrawable.start();
//            image_heater_help.setImageDrawable(gifDrawable);
//        }
//
//        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        //点击空白处时，隐藏掉pop窗口
//        popupWindow2.setFocusable(true);
//        popupWindow2.setOutsideTouchable(true);
//        //添加弹出、弹入的动画
//        popupWindow2.setAnimationStyle(R.style.Popupwindow);
//        backgroundAlpha(0.6f);
//        popupWindow2.setFocusable(false);
//        popupWindow2.setOutsideTouchable(false);
////        ColorDrawable dw = new ColorDrawable(0x30000000);
////        popupWindow.setBackgroundDrawable(dw);
//        popupWindow2.showAsDropDown(et_pswd, 0, -20);
////        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
//        //添加按键事件监听
//        popupWindow2.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                backgroundAlpha(1.0f);
//            }
//        });
//    }


    @Override
    protected void onResume() {
        super.onResume();
        permissionGrantedSuccess();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mReceiverRegistered) {
                unregisterReceiver(mReceiver);
            }

            if (bound) {
                unbindService(connection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private boolean mReceiverRegistered = false;

    private boolean isSDKAtLeastP() {
        return Build.VERSION.SDK_INT >= 28;
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (isSDKAtLeastP()) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 把执行结果的操作给EasyPermissions
        System.out.println(requestCode);
        if (isNeedCheck) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    private static final int RC_CAMERA_AND_LOCATION = 0;

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void permissionGrantedSuccess() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {

        } else {
//             没有申请过权限，现在去申请
            EasyPermissions.requestPermissions(this, getString(R.string.location),
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    boolean isNeedCheck = true;

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog
                    .Builder(this)
                    .setTitle("提示")
                    .setRationale("请点击\"设置\"打开定位权限。")
                    .setPositiveButton("设置")
                    .setNegativeButton("取消")
                    .build()
                    .show();
            isNeedCheck = false;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            assert wifiManager != null;
            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    WifiInfo wifiInfo;
                    if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
                        wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    } else {
                        wifiInfo = wifiManager.getConnectionInfo();
                    }
                    onWifiChanged(wifiInfo);
                    break;
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    onWifiChanged(wifiManager.getConnectionInfo());
                    break;
            }
        }
    };
    String bSsid;

    private void onWifiChanged(WifiInfo info) {

        if (info == null) {
            et_ssid.setText("");
            et_pswd.setText("");
            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi已中断，请连接WiFi重新配置");
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
            if (popupWindow2 != null && popupWindow2.isShowing()) {
                et_ssid.setEnabled(true);
                et_pswd.setEnabled(true);
                btn_match.setEnabled(true);
                popupWindow2.dismiss();
                backgroundAlpha(1f);
            }
        } else {
            String apSsid = info.getSSID();
            bSsid = info.getBSSID();
            if (apSsid.startsWith("\"") && apSsid.endsWith("\"")) {
                apSsid = apSsid.substring(1, apSsid.length() - 1);
                if ("<unknown ssid>".equals(apSsid)) {
                    et_ssid.setText("");
                    et_pswd.setText("");
                }
            }

            if (wifi.contains(apSsid)) {
                et_ssid.setText(apSsid);
                String pswd = wifi.getString(apSsid, "");
                et_pswd.setText(pswd);
            } else {
                et_ssid.setText(apSsid);
                et_pswd.setText("");
                if ("<unknown ssid>".equals(apSsid)) {
                    et_ssid.setText("");
                    et_pswd.setText("");
                }
            }
            if (!TextUtils.isEmpty(apSsid)) {
                if (apSsid.contains("+") || apSsid.contains("/") || apSsid.contains("#")) {
                    et_ssid.setText("");
                    ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi名称为不含有+/#特殊符号的英文");
                } else {
                    char[] chars = apSsid.toCharArray();
                    et_ssid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi名称不可编辑");
                        }
                    });

                    for (char c : chars) {
                        if (IsChinese.isChinese(c)) {
                            ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi名称不能是中文");
                            et_ssid.setText("");
                            et_pswd.setText("");
                            break;
                        }
                    }
                }
            } else {
                et_ssid.setText("");
                et_ssid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "请连接英文名称的wifi");
                    }
                });
                et_pswd.setText("");
            }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    int frequence = info.getFrequency();
//                    if (frequence > 4900 && frequence < 5900) {
//                        // Connected 5G wifi. Device does not support 5G
//                        et_ssid.setText("");
//                        et_ssid.setHint("不支持5G WiFi");
//                        et_pswd.setText("");
//                    }
//                }
            if (isMatching && !TextUtils.isEmpty(wifiName) && !wifiName.equals(apSsid)) {
                isMatching = false;
                wifiName = "";
                ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi已切换,请重新配置");
                if (mEsptouchTask != null) {
                    mEsptouchTask.interrupt();
                }
                if (popupWindow2 != null && popupWindow2.isShowing()) {
                    et_ssid.setEnabled(true);
                    et_pswd.setEnabled(true);
                    btn_match.setEnabled(true);
                    popupWindow2.dismiss();
                    backgroundAlpha(1f);
                }
            }
        }
    }
}