package com.peihou.warmer.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.annotation.Nullable;

import com.peihou.warmer.R;

public class TimeTaskCircleBar extends View {
    private Paint paint;//定义一个画笔
    private Paint mPaint;
    private float ring_width;//圆环宽度
    private float ring_dot;//圆环上分布的锚点
    private int ring_color;//圆环颜色
    private boolean touch_enable;//能否触摸
    private float current_angle=0;//当前角度
    float centerX;
    float centerY;
    public TimeTaskCircleBar(Context context) {
        this(context,null);
    }

    public TimeTaskCircleBar(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public TimeTaskCircleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
        initPaint();
    }
    private void initPaint() {
        paint = new Paint();
//        给画笔设置颜色
        paint.setColor(Color.RED);
//        设置画笔属性
//        paint.setStyle(Paint.Style.FILL);//画笔属性是实心圆
        paint.setStyle(Paint.Style.STROKE);//画笔属性是空心圆
        paint.setStrokeWidth(ring_width);//设置画笔粗细
        paint.setAntiAlias(true);
        mPaint=new Paint();
        mPaint.setColor(getResources().getColor(R.color.white));

    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCircleProgressBar, defStyle, 0);
        ring_width = a.getDimension(R.styleable.CustomCircleProgressBar_ring_width, getDimen(R.dimen.dp_20));
        ring_dot=a.getDimension(R.styleable.CustomCircleProgressBar_ring_dot,getDimen(R.dimen.dp_2));
        ring_color = a.getColor(R.styleable.CustomCircleProgressBar_ring_color, getResources().getColor(R.color.blue));
        touch_enable = a.getBoolean(R.styleable.CustomCircleProgressBar_touch_enable, false);
        current_angle = a.getFloat(R.styleable.CustomCircleProgressBar_current_angle, 0);
        a.recycle();
    }

    private float getDimen(int dimenId) {
        return getResources().getDimension(dimenId);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();


        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = width - getPaddingRight();
        float bottom = height - getPaddingBottom();
        centerX = (left + right) / 2;
        centerY = (top + bottom) / 2;

        /*画圆环*/
        paint.setStyle(Paint.Style.STROKE);//画笔属性是空心圆
        paint.setColor(getResources().getColor(R.color.color_blank3));
        float wheelRadius = (canvas.getWidth() - getPaddingLeft() - getPaddingRight()) / 2 - ring_width / 2;
        canvas.drawCircle(centerX,centerY,wheelRadius,paint);
        canvas.save();

        paint.setColor(getResources().getColor(R.color.white));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        Log.i("ring_dot","-->"+ring_dot);
        for (int i = 0; i < 24; i++) {//总共24个点  所以绘制24次  绘制24小时时间刻度点
            if (i % 2 == 0) {

                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -7, getResources().getDisplayMetrics()),
                        getPaddingTop() + ring_dot + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8, getResources().getDisplayMetrics()),
                        getPaddingTop() + ring_dot + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1, getResources().getDisplayMetrics()), paint);
            } else {
                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -10, getResources().getDisplayMetrics()),
                        getPaddingTop() + ring_dot + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -8, getResources().getDisplayMetrics()),
                        getPaddingBottom() + ring_dot + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -3, getResources().getDisplayMetrics()), paint);
            }
            canvas.rotate(15, centerX, centerY);//360度  绘制60次   每次旋转6度
        }
        canvas.save();

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        String[] strs = new String[]{"24","2","4","6", "8","10", "12","14", "16","18", "20","22"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
        Rect rect = new Rect();
        canvas.save();


        for (int i = 0; i < 12; i++) {//绘制12次  每次旋转30度
            mPaint.getTextBounds(strs[i], 0, strs[i].length(), rect);
            canvas.drawText(strs[i], centerX ,
                    getPaddingTop() + ring_dot + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()), mPaint);
            canvas.rotate(30, centerX, centerY);
        }

    }
}
