package com.peihou.warmer.base;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;


import com.bumptech.glide.Glide;
import com.mob.MobSDK;
import com.peihou.warmer.database.dao.impl.DeviceDaoImpl;
import com.peihou.warmer.lib.DaemonHolder;
import com.peihou.warmer.pojo.Device;
import com.peihou.warmer.service.MQService;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;




/**
 * Created by hongming.wang on 2018/1/23.
 */

public class MyApplication extends Application {
    public static String update="cancel";
    private int count = 0;
    private List<Activity> activities;
    private static Context mContext;
    public static int floating=0;

    private DeviceDaoImpl deviceDao;

    public static Context getContext(){
        return mContext;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        DaemonHolder.init(this, MQService.class);
        mContext = getApplicationContext();
        new LoadAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        activities=new ArrayList<>();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count ++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if(count > 0) {
                    count--;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
    class LoadAsync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            MobSDK.init(mContext);
            Glide.get(mContext);
            deviceDao=new DeviceDaoImpl(mContext);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public DeviceDaoImpl getDeviceDao() {
        return deviceDao;
    }

    //    private static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID";
//    private static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME";
//    //需要创建 NotificationChannel
//    private void createNotificationChannel(){
//        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        //判断是不是 Android8.0
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel = new NotificationChannel(
//                    //字符串类型的 Channel id
//                    PUSH_CHANNEL_ID,
//                    //字符串类型的 Channel name
//                    PUSH_CHANNEL_NAME,
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            manager.createNotificationChannel(channel);
//        }
//    }
    public void addActivity(Activity activity){
        if (!activities.contains(activity)){
            activities.add(activity);
        }
    }


    public void removeActivity(Activity activity){
        if (activities.contains(activity)){
            activities.remove(activity);
            activity.finish();
        }
    }
    public void removeActiviies(List<Activity> activities){
        for (Activity activity:activities){
            removeActivity(activity);
        }
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void removeAllActivity(){
        for (Activity activity:activities){
            activity.finish();
        }
        List<Device> devices=deviceDao.findAllDevice();
        for (Device device:devices){
            device.setOnline(false);
            deviceDao.update(device);
        }
    }
    /**
     * 判断app是否在后台
     * @return
     */
    public boolean isBackground(){
        if(count <= 0){
            return true;
        } else {
            return false;
        }
    }

//    /**
//     * 反射 禁止弹窗
//     */
//    private void disableAPIDialog(){
//
//        try {
//            if (Build.VERSION.SDK_INT >= 28){
//                Class clazz = Class.forName("android.app.ActivityThread");
//                Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
//                currentActivityThread.setAccessible(true);
//                Object activityThread = currentActivityThread.invoke(null);
//                Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
//                mHiddenApiWarningShown.setAccessible(true);
//                mHiddenApiWarningShown.setBoolean(activityThread, true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
