package com.peihou.warmer.custom.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.peihou.warmer.R;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DialogLoad extends Dialog {

    Unbinder unbinder;
    private int load;
    Context context;
    @BindView(R.id.layout)
    RelativeLayout layout;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;//加载图片
    @BindView(R.id.tv_load) TextView tv_load;
    public DialogLoad(@NonNull Context context) {
        super(context,R.style.MyDialog);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_load);
        unbinder= ButterKnife.bind(this);
    }
//    AnimationDrawable animationDrawable;
    @Override
    protected void onStart() {
        super.onStart();
       startAnim();
//        img_load.setImageResource(R.drawable.load);
//        animationDrawable = (AnimationDrawable) img_load.getDrawable();
//        animationDrawable.start();
        if (load!=0){
            tv_load.setText(load);
        }
        if (back==1){
            layout.setBackground(context.getResources().getDrawable(R.drawable.shape_load2));
        }else {
            layout.setBackground(context.getResources().getDrawable(R.drawable.shape_load));
        }
    }

    int outside=0;

    public int getOutside() {
        return outside;
    }

    public void setOutside(int outside) {
        this.outside = outside;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (outside==1 && event.getAction()== KeyEvent.ACTION_DOWN){
            return false;
        }
        return true;
    }

    private int back=0;

    public int getBack() {
        return back;
    }

    public void setBack(int back) {
        this.back = back;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (animationDrawable!=null){
//            animationDrawable.stop();
//        }
       stopAnim();
        unbinder.unbind();
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getLoad() {
        return load;
    }
    void startAnim(){
        avi.show();
        // or avi.smoothToShow();
    }

    void stopAnim(){
        avi.hide();
        // or avi.smoothToHide();
    }
}
