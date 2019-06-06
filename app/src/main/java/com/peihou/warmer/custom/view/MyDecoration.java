package com.peihou.warmer.custom.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MyDecoration extends RecyclerView.ItemDecoration {
    private float mDididerHeight=1;//线的高度
    private Paint mPaint;//画笔将自己出来的分割线矩形画出颜色
    private float margin=0;//左右偏移量

    public MyDecoration(){
        mPaint=new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(Color.GRAY);//默认颜色
    }
    //通过建造者模式来设置三个属性

    /**
     * 设置左右偏移量
     * @param margin
     * @return
     */
    public MyDecoration setMargin(float margin){
        this.margin=margin;
        return this;
    }

    /**
     * 设置分割线颜色
     * @param color
     * @return
     */
    public MyDecoration setColor(int color){
        mPaint.setColor(color);
        return this;
    }

    /**
     * 设置分割线高度
     * @param height
     * @return
     */
    public MyDecoration setDeiverHeight(float height){
        this.mDididerHeight=height;
        return this;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //第一个itemView不需要在上面绘制分割线
        if (parent.getChildAdapterPosition(view)!=0){
            outRect.top= (int) mDididerHeight;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount=parent.getChildCount();
        for (int i = 0; i <childCount ; i++) {
            View view=parent.getChildAt(i);
            int index=parent.getChildAdapterPosition(view);
            //第一个itemView不需要绘制
            if (index==0){
                continue;
            }
            float dividerTop=view.getTop()-mDididerHeight;
            float dividerLeft=parent.getPaddingLeft()+margin;
            float dividerBottom=view.getTop();
            float dividerRight=parent.getWidth()-parent.getPaddingRight()-margin;
            c.drawRect(dividerLeft,dividerTop,dividerRight,dividerBottom,mPaint);
        }
    }
}
