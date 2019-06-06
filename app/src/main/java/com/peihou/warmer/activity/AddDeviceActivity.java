package com.peihou.warmer.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.TextView;


import com.peihou.warmer.R;
import com.peihou.warmer.base.BaseActivity;
import com.peihou.warmer.esptouch.EspWifiAdminSimple;
import com.peihou.warmer.esptouch.EsptouchTask;
import com.peihou.warmer.esptouch.IEsptouchListener;
import com.peihou.warmer.esptouch.IEsptouchResult;
import com.peihou.warmer.esptouch.IEsptouchTask;
import com.peihou.warmer.esptouch.task.__IEsptouchTask;
import com.peihou.warmer.http.NetWorkUtil;
import com.peihou.warmer.service.MQService;
import com.peihou.warmer.utils.IsChinese;
import com.peihou.warmer.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddDeviceActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{

    @BindView(R.id.btn_match)
    Button btn_match;
    @BindView(R.id.img_back)
    ImageView img_back;
    GifDrawable gifDrawable;


    @BindView(R.id.tv_wifi)
    TextView et_ssid;
    @BindView(R.id.et_pswd) EditText et_pswd;
    private int match = 0;

    private String wifiName;
    private boolean isMatching = false;

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
                    if (gifDrawable != null && gifDrawable.isRunning()) {
                        gifDrawable.stop();
                    }
                    popupWindow2.dismiss();
                    backgroundAlpha(1f);
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
                        if (!TextUtils.isEmpty(ssid)) {
//                    popupWindow();
                            match = 1;
                            et_ssid.setEnabled(false);
                            et_pswd.setEnabled(false);
                            btn_match.setEnabled(false);
                            popupmenuWindow3();
                            wifiName = ssid;
                            isMatching = true;
                            new EsptouchAsyncTask3().execute(ssid, apBssid, apPassword, taskResultCountStr);
                        }
                    } else {
                        ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "请检查网络");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }


    @Override
    public void initParms(Bundle parms) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_device;
    }
    SharedPreferences wifi;
    @Override
    public void initView() {
        wifi= getSharedPreferences("wifi", MODE_PRIVATE);
        mWifiAdmin = new EspWifiAdminSimple(this);
        et_ssid.setFocusable(false);
        registerBroadcastReceiver();
        Intent service = new Intent(AddDeviceActivity.this, MQService.class);
        bound = bindService(service, connection, Context.BIND_AUTO_CREATE);
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
                        String deviceMac="";
                        Log.i("diff", "-->" + System.currentTimeMillis());
                        for (IEsptouchResult resultInList : result) {
                            ssid = resultInList.getBssid();
                            Log.i("ssidssid", "-->" + ssid);
                            sb.append("配置成功");
                            String wifiName = et_ssid.getText().toString();
                             deviceMac= wifiName + ssid;

                             if (!TextUtils.isEmpty(deviceMac)){
                                 popupWindow2.dismiss();
                                 ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this,deviceMac);
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
                        if (gifDrawable != null && gifDrawable.isPlaying()) {
                            gifDrawable.stop();

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
                        }
                        match = 0;
                        popupWindow2.dismiss();
                        backgroundAlpha(1f);
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
                isMatching = false;
                wifiName = "";
                if (gifDrawable != null && gifDrawable.isRunning()) {
                    gifDrawable.stop();
                }
                popupWindow2.dismiss();
                backgroundAlpha(1f);
                return false;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    PopupWindow popupWindow2;
    GifImageView image_heater_help;
    public void popupmenuWindow3() {
        if (popupWindow2 != null && popupWindow2.isShowing()) {
            return;
        }
        View view = View.inflate(this, R.layout.popup_help2, null);
        image_heater_help = (GifImageView) view.findViewById(R.id.image_heater_help);
        try {
            gifDrawable = new GifDrawable(getResources(), R.mipmap.touxiang3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        image_heater_help.setVisibility(View.VISIBLE);
        if (gifDrawable != null) {
            gifDrawable.start();
            image_heater_help.setImageDrawable(gifDrawable);
        }

        popupWindow2 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //点击空白处时，隐藏掉pop窗口
        popupWindow2.setFocusable(true);
        popupWindow2.setOutsideTouchable(true);
        //添加弹出、弹入的动画
        popupWindow2.setAnimationStyle(R.style.Popupwindow);
        backgroundAlpha(0.6f);
        popupWindow2.setFocusable(false);
        popupWindow2.setOutsideTouchable(false);
//        ColorDrawable dw = new ColorDrawable(0x30000000);
//        popupWindow.setBackgroundDrawable(dw);
        popupWindow2.showAsDropDown(et_pswd, 0, -20);
//        popupWindow.showAtLocation(tv_home_manager, Gravity.RIGHT, 0, 0);
        //添加按键事件监听
        popupWindow2.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
    }


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

    boolean isNeedCheck=true;
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
                    if (gifDrawable != null && gifDrawable.isRunning()) {
                        gifDrawable.stop();
                    }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int frequence = info.getFrequency();
                    if (frequence > 4900 && frequence < 5900) {
                        // Connected 5G wifi. Device does not support 5G
                        et_ssid.setText("");
                        et_ssid.setHint("不支持5G WiFi");
                        et_pswd.setText("");
                    }
                }
                if (isMatching && !TextUtils.isEmpty(wifiName) && !wifiName.equals(apSsid)) {
                    isMatching = false;
                    wifiName = "";
                    ToastUtils.INSTANCE.toastShort(AddDeviceActivity.this, "WiFi已切换,请重新配置");
                    if (mEsptouchTask != null) {
                        mEsptouchTask.interrupt();
                    }
                    if (popupWindow2 != null && popupWindow2.isShowing()) {
                        if (gifDrawable != null && gifDrawable.isRunning()) {
                            gifDrawable.stop();
                        }
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