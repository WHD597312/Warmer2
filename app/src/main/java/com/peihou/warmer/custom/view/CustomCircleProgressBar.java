package com.peihou.warmer.custom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.peihou.warmer.R;


public class CustomCircleProgressBar extends View {
    private static final double RADIAN = 180 / Math.PI;
    private Paint paint;//定义一个画笔
    private float ring_width;//圆环宽度
    private int ring_color;//圆环颜色
    private boolean touch_enable;//能否触摸
    private float current_angle;//当前角度
     float centerX;
     float centerY;

    public CustomCircleProgressBar(Context context) {
        this(context,null);
    }

    public CustomCircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CustomCircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs,defStyleAttr);
        initPaint();
    }

    private void initPaint(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        给画笔设置颜色
        paint.setColor(Color.RED);
//        设置画笔属性
//        paint.setStyle(Paint.Style.FILL);//画笔属性是实心圆
        paint.setStyle(Paint.Style.STROKE);//画笔属性是空心圆
        paint.setStrokeWidth(ring_width);//设置画笔粗细
        paint.setAntiAlias(true);

    }
    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.CustomCircleProgressBar,defStyle,0);
        ring_width=a.getDimension(R.styleable.CustomCircleProgressBar_ring_width,getDimen(R.dimen.dp_5));
        ring_color=a.getColor(R.styleable.CustomCircleProgressBar_ring_color,getResources().getColor(R.color.blue));
        touch_enable=a.getBoolean(R.styleable.CustomCircleProgressBar_touch_enable,false);
        current_angle=a.getFloat(R.styleable.CustomCircleProgressBar_current_angle,0);
        a.recycle();
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
        int width=getWidth();
        int height=getHeight();


        float left = getPaddingLeft()+ring_width;
        float top = getPaddingTop()+ring_width;
        float right = width - getPaddingRight()-ring_width ;
        float bottom = height - getPaddingBottom()-ring_width;
       centerX = (left + right) / 2;
       centerY = (top + bottom) / 2;
        float radius=canvas.getWidth()/2-ring_width;
        paint.setColor(getResources().getColor(R.color.gray));
        canvas.drawCircle(centerX, centerY, radius, paint);
        canvas.save();
        Log.i("current_angle","-->"+current_angle);
        paint.setColor(Color.RED);
        canvas.rotate(180,width/2,height/2);
        canvas.drawArc(new RectF(left,top,right,bottom),0,current_angle,false,paint);
        canvas.save();

        int x0=width/2;
        int y0=height/2;
        float R = (width)/2-ring_width;
        float Point_x= (float) (x0+R*Math.cos(current_angle*3.14/180));
        float Point_y= (float) (y0+R*Math.sin(current_angle * 3.14 / 180));

        paint.setColor(Color.BLUE);
        canvas.drawCircle(Point_x,Point_y,10,paint);

    }
    private float getDimen(int dimenId) {
        return getResources().getDimension(dimenId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float Action_x = event.getX();
        float Action_y = event.getY();
        /*根据坐标转换成对应的角度*/
        float get_x0 = Action_x - centerX;
        float get_y0 = Action_y - centerY;
        /*01：左下角区域*/

        Log.i("get_x0","("+get_x0+","+get_y0+")");

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                //左下角
                if(get_x0<=0&get_y0>=0){
                    float tan_x = get_x0 * (-1);
                    float tan_y = get_y0;
                    double atan = Math.atan(tan_x / tan_y);
                    current_angle= (int) Math.toDegrees(atan)+270;
                    Log.i("current_angle","左下角-->"+current_angle);
                }

                //左上角

                if(get_x0<=0&get_y0<=0){
                    float tan_x = get_x0 * (-1);
                    float tan_y = get_y0*(-1);
                    double atan = Math.atan(tan_y / tan_x);
                    current_angle= (int) Math.toDegrees(atan);
                    Log.i("current_angle","左上角-->"+current_angle);
                }

                //右上角

                if(get_x0>=0&get_y0<=0){
                    float tan_x = get_x0 ;
                    float tan_y = get_y0*(-1);
                    double atan = Math.atan(tan_x/ tan_y);
                    current_angle= (int) Math.toDegrees(atan)+90;
                    Log.i("current_angle","右上角-->"+current_angle);
                }

                //右下角

                if(get_x0>=0&get_y0>=0){
                    float tan_x = get_x0 ;
                    float tan_y = get_y0;
                    double atan = Math.atan(tan_y / tan_x);
                    current_angle= (int) Math.toDegrees(atan)+180;
                    Log.i("current_angle","右下角-->"+current_angle);
                }
                invalidate();
//                invalidate();
                break;
        }
        return true;
    }

}
