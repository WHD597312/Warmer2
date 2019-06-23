package com.peihou.warmer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.peihou.warmer.base.MyApplication;
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl;
import com.peihou.warmer.pojo.Device;
import com.peihou.warmer.service.MQService;
import com.peihou.warmer.utils.ToastUtils;

import java.util.List;


public class MQTTMessageReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


//        if (intent!=null && intent.getAction().equals(Intent.ACTION_TIME_TICK)){
//            boolean running2 = ServiceUtils.isServiceRunning(context, "com.peihou.willgood2.service.MQService");
//            if (!running2) {
//                Intent intent2 = new Intent(context, MQService.class);
//                intent2.putExtra("restart", 1);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    context.startForegroundService(intent2);
//                }else {
//                    context.startService(intent2);
//                }
//            }
//        }
        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            MyApplication application= (MyApplication) MyApplication.getContext();
            DeviceDaoImpl deviceDao=application.getDeviceDao();
            if (deviceDao!=null){
                List<Device> devices=deviceDao.findAllDevice();
                for (Device device:devices){
                    device.setOnline(false);
                    deviceDao.update(device);
                }
            }
            ToastUtils.INSTANCE.toastShort(context,"网络不可用");
            Intent noNet = new Intent("offline");
            context.sendBroadcast(noNet);
            //改变背景或者 处理网络的全局变量
        } else if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(mqttIntent);
//            }else {
//                context.startService(mqttIntent);
//            }
        }
    }

}
