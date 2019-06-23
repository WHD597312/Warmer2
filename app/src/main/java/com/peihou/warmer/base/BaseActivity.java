package com.peihou.warmer.base;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


import com.peihou.warmer.R;
import com.peihou.warmer.custom.dialog.DialogLoad;
import com.peihou.warmer.lib.DaemonHolder;
import com.peihou.warmer.utils.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

public abstract class BaseActivity extends Activity {
    /** 是否沉浸状态栏 **/
    private boolean isSetStatusBar = false;
    /** 是否允许全屏 **/
    private boolean mAllowFullScreen = false;
    /** 是否禁止旋转屏幕 **/
    private boolean isAllowScreenRoate = true;

    Unbinder unbinder;
    protected MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        application = (MyApplication) getApplication();
        application.addActivity(this);

        Log.i("BaseActivity","-->"+"onCreate");
//        if (getSupportActionBar() != null){
//            getSupportActionBar().hide();
//        }
        try {
            Bundle bundle = getIntent().getExtras();
            if(bundle!=null)
                initParms(bundle);
//            mContextView =View.inflate(this,bindLayout(),null);
            setStatusTextColor(this);
            setContentView(bindLayout());
//            setContentView(mContextView);
            unbinder= ButterKnife.bind(this);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            initView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置状态栏文字色值为深色调
     *
     * @param
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void setStatusTextColor(Activity activity) {
        boolean useDart=isStatusTextColor(true);
        if (isFlyme()) {
            //魅族
            processFlyMe(useDart, activity);
        } else if (isMIUI()) {
            //小米
            setMIUIStatusTextColor(activity, useDart);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
            //OPPO
            setOPPOStatusTextColor();
        } else {
            //其他
            setOtherStatusTextColor();
        }
    }

    /**
     * 判断手机是否是魅族
     *
     * @return
     */
    private static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private static void processFlyMe(boolean isLightStatusBar, Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (isLightStatusBar) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    /**
     * 判断手机是否是小米
     *
     * @return
     */
    private static boolean isMIUI() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                return true;
            }
        } catch (final IOException e) {
            return false;
        }
        return false;
    }

    /**
     * 小米手机更改状态栏颜色
     *
     * @param activity
     * @param useDart
     */
    private static void setMIUIStatusTextColor(Activity activity, boolean useDart) {
        //6.0后小米状态栏用的原生的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (useDart) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, 0);
        } else {
            processMIUI(useDart, activity);
        }
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上  lightStatusBar为真时表示黑色字体
     */
    private static void processMIUI(boolean lightStatusBar, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), lightStatusBar ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 设置OPPO手机状态栏字体为黑色(colorOS3.0,6.0以下部分手机)
     *
     * @param lightStatusBar
     * @param activity
     */
    private static final int SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010;

    private void setOPPOStatusTextColor() {
       initWindows();

    }

    /**
     * 其他手机更改状态栏字体颜色
     */
    private  void setOtherStatusTextColor() {
        initWindows();
    }
    boolean statusTextColor=false;
    protected boolean isStatusTextColor(boolean status){
        statusTextColor=status;
        return status;
    }
    protected int setStatusColor(int color){
        return color;
    }
    private void initWindows() {
        Window window = getWindow();
//        int color = Color.parseColor("#E0E0E0");
        int color=setStatusColor(Color.parseColor("#00ffffff"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP &&Build.VERSION.SDK_INT<Build.VERSION_CODES.M ){
                if (color==Color.parseColor("#00ffffff") || color==Color.parseColor("#ffffff")){
                    color = Color.parseColor("#E0E0E0");
                }
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (statusTextColor){
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(color);
//            //设置导航栏颜色
//            window.setNavigationBarColor(color);
            ViewGroup contentView = ((ViewGroup) findViewById(android.R.id.content));
            View childAt = contentView.getChildAt(0);
            if (childAt != null) {
                childAt.setFitsSystemWindows(false);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //设置contentview为fitsSystemWindows
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            View childAt = contentView.getChildAt(0);
            if (childAt != null) {
                childAt.setFitsSystemWindows(true);
            }
            //给statusbar着色
            View view = new View(this);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(this)));
            view.setBackgroundColor(color);
            contentView.addView(view);
        }
    }
    //设置蒙版
    protected void backgroundAlpha(float f) {
        WindowManager.LayoutParams lp =getWindow().getAttributes();
        lp.alpha = f;
        getWindow().setAttributes(lp);
    }
    @Override
    protected void onStart() {
        super.onStart();
        DaemonHolder.startService();
//        initWindows();
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }
    @Override
    public void onBackPressed() {
        Log.i("sssss","-->onBackPressed2");
        super.onBackPressed();
        if (application!=null){
            Log.i("sssss","-->onBackPressed");
            application.removeActivity(this);
        }
    }


    /**
     * [初始化Bundle参数]
     *
     * @param parms
     */
    public abstract void initParms(Bundle parms);

    /**
     * [绑定布局]
     *
     * @return
     */
    public abstract int bindLayout();


    /**
     * [重写： 1.是否沉浸状态栏 2.是否全屏 3.是否禁止旋转屏幕]
     */
    // public abstract void setActivityPre();

    /**
     * [初始化控件]
     *
     * @param
     */
    public abstract void initView();
    int load = R.string.load;
    protected DialogLoad dialogLoad;
    protected int back=0;
    protected void setLoadDialog() {
        if (dialogLoad != null && dialogLoad.isShowing()) {
            return;
        }
        dialogLoad = new DialogLoad(this);
        dialogLoad.setBack(back);
        dialogLoad.setCanceledOnTouchOutside(false);
//        dialogLoad.setCanceledOnTouchOutside(true);
        dialogLoad.setLoad(load);
        dialogLoad.show();
//        backgroundAlpha(0.6f);
    }
    public DownCountTime countTime=new DownCountTime(2000,1000);
    public class DownCountTime extends CountDownTimer{

        public DownCountTime(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            setLoadDialog();
        }

        @Override
        public void onFinish() {
            if (dialogLoad!=null && dialogLoad.isShowing()){
                dialogLoad.dismiss();
            }
        }
    }
    protected boolean isDialogShow(){
        if (dialogLoad!=null && dialogLoad.isShowing()){
            ToastUtils.INSTANCE.toastShort(this,"请稍后...");
            return true;
        }
        return false;
    }

    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {
        startActivity(clz, null);
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        MyApplication.getQueue().cancelAll(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * [是否允许全屏]
     *
     * @param allowFullScreen
     */
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.mAllowFullScreen = allowFullScreen;
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     */
    public void setSteepStatusBar(boolean isSetStatusBar) {
        Log.e("qqqqqIIII",isSetStatusBar+","+this.isSetStatusBar);
        this.isSetStatusBar = isSetStatusBar;
    }

    /**
     * [是否允许屏幕旋转]
     *
     * @param isAllowScreenRoate
     */
    public void setScreenRoate(boolean isAllowScreenRoate) {
        this.isAllowScreenRoate = isAllowScreenRoate;
    }
}