package com.peihou.warmer.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.support.annotation.Nullable;

import com.peihou.warmer.R;

public class WarmerProgressBar extends View {
    private Paint paint;//定义一个画笔
    private float ring_width;//圆环宽度
    private int ring_color;//圆环颜色
    private boolean isCanTouch;//能否触摸
    private float current_angle=0;//当前角度
    float centerX;
    float centerY;
    private int rangRadus=0;
    public WarmerProgressBar(Context context) {
        this(context, null);
    }

    public WarmerProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WarmerProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        paint.setStyle(Paint.Style.FILL_AND_STROKE);//画笔属性是空心圆
        paint.setStrokeWidth(ring_width);//设置画笔粗细
        paint.setAntiAlias(true);

    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCircleProgressBar, defStyle, 0);
        ring_width = a.getDimension(R.styleable.CustomCircleProgressBar_ring_width, getDimen(R.dimen.dp_5));
        ring_color = a.getColor(R.styleable.CustomCircleProgressBar_ring_color, getResources().getColor(R.color.white));
        isCanTouch = a.getBoolean(R.styleable.CustomCircleProgressBar_touch_enable, false);
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
        rangRadus=width/2;
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
//        float radius = canvas.getWidth() / 2 - ring_width;
        canvas.rotate(225,centerX,centerY);
        paint.setColor(getResources().getColor(R.color.white));
        for (int i = 0; i < 45; i++) {//总共45个点  所以绘制45次  //绘制一圈的小黑点

            if (i==0){
                paint.setColor(getResources().getColor(R.color.color_orange));
            }else if (i>0 && i<=34){
                paint.setColor(getResources().getColor(R.color.white));
            }else if (i>34){
                paint.setColor(getResources().getColor(R.color.color_blank2));

            }
            canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                    getPaddingTop() + ring_width + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                    centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                    getPaddingTop() + ring_width + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), paint);
            canvas.rotate(8, centerX, centerY);
        }

        Log.i("current_angle","-->"+current_angle);
        if (current_angle>=0){
            paint.setColor(getResources().getColor(R.color.color_orange));
            for (int i = 0; i <current_angle/8 ; i++) {

                canvas.drawRect(centerX - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                        getPaddingTop() + ring_width + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        centerX + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        getPaddingTop() + ring_width + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), paint);
                canvas.rotate(8, centerX, centerY);
            }
            canvas.save();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float Action_x = event.getX();
        float Action_y = event.getY();
        /*根据坐标转换成对应的角度*/
        float get_x0 = Action_x - centerX;
        float get_y0 = Action_y - centerY;
        /*01：左下角区域*/

        Log.i("get_x0", "(" + get_x0 + "," + get_y0 + ")");

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //左下角
                if (isInCiecle(Action_x,Action_y)){
                    if (get_x0 <= 0 & get_y0 >= 0) {
                        Log.i("current_angle", "左下角-->" + current_angle);
                        float tan_x = get_x0 * (-1);
                        float tan_y = get_y0;
                        double atan = Math.atan(tan_x / tan_y);

                        current_angle = (int) Math.toDegrees(atan) - 45;
                        Log.i("current_angle", "左下角-->" + current_angle);
                        if (current_angle < 0) {
                            break;
                        }
                    }

                    //左上角

                    if (get_x0 <= 0 & get_y0 <= 0) {
                        float tan_x = get_x0 * (-1);
                        float tan_y = get_y0 * (-1);
                        double atan = Math.atan(tan_y / tan_x);
                        current_angle = (int) Math.toDegrees(atan) + 45;
                        Log.i("current_angle", "左上角-->" + current_angle);
                    }

                    //右上角

                    if (get_x0 >= 0 & get_y0 <= 0) {
                        float tan_x = get_x0;
                        float tan_y = get_y0 * (-1);
                        double atan = Math.atan(tan_x / tan_y);
                        current_angle = (int) Math.toDegrees(atan) + 135;
                        Log.i("current_angle", "右上角-->" + current_angle);
                    }

                    //右下角

                    if (get_x0 >= 0 & get_y0 >= 0) {

                        float tan_x = get_x0;
                        float tan_y = get_y0;
                        double atan = Math.atan(tan_y / tan_x);
                        current_angle = (int) Math.toDegrees(atan);
                        Log.i("current_angle", "右下角-->" + current_angle);

                        if (current_angle >= 0 && current_angle <= 50) {
                            current_angle = current_angle + 225;
                        } else if (current_angle > 50) {
                            break;
                        }
                        Log.i("current_angle", "右下角-->" + current_angle);
                    }
                    invalidate();
                }

//                invalidate();
                break;
        }
        return true;
    }

    /**判断落点是否在圆环上*/
    public boolean isInCiecle(float x,float y){
        Log.i("x","-->"+x);
        Log.i("y","-->"+y);
        float distance = (float) Math.sqrt((x-rangRadus)*(x-rangRadus)+(y-rangRadus)*(y-rangRadus));
        Log.i("distance","-->"+distance);
        int smallCircleRadus=rangRadus/2+50;
        Log.i("smallCircleRadus","-->"+smallCircleRadus);
        if (distance>=smallCircleRadus && distance<=rangRadus)
            return true;
        else
            return false;
    }
    public boolean isCanTouch() {
        return isCanTouch;
    }

    /**
     * 能够触摸滑动盘
     * @param canTouch
     */
    public void setCanTouch(boolean canTouch) {
        isCanTouch = canTouch;
    }
}
