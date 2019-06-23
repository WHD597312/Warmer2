package com.peihou.warmer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.peihou.warmer.activity.Set01Activity;
import com.peihou.warmer.http.WeakRefHandler;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends AppCompatActivity {

    public Main2Activity() {
        handler = new WeakRefHandler(mCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=new Intent(this, Set01Activity.class);
        startActivity(intent);
//        setContentView(R.layout.activity_main2);
//        Intent intent=new Intent();
//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Ringtone r = RingtoneManager.getRingtone(this, notification);
//        r.play();
//        String share="123456";
//        try {
//            share=new String(Base64.encode(share.getBytes("utf-8"), Base64.NO_WRAP),"UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
//    @OnClick({R.id.button3,R.id.button4})
//    public void onClick(View view){}
    ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    Handler handler;
    Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            return true;
        }
    };


    class TimerAdapter extends RecyclerView.Adapter<ViewHolder>{

        private Context context;

        public TimerAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view=View.inflate(context,R.layout.item_timer,null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.tv_count.setText(""+i);
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.rl_timer) RelativeLayout rl_timer;
        @BindView(R.id.tv_count) TextView tv_count;

        @BindView(R.id.tv_timer) TextView tv_timer;

        @BindView(R.id.tv_temp) TextView tv_temp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }
}
