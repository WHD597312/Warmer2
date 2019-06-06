package com.peihou.warmer.custom.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


import com.peihou.warmer.R;

import java.lang.reflect.Field;

public class Timepicker extends NumberPicker {
    public Timepicker(Context context, AttributeSet attrs,
                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    public Timepicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Timepicker(Context context) {
        super(context);

    }

    @Override
    public void addView(View child) {
        this.addView(child, null);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        this.addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setNumberPicker(child);
        setNumberPickerDividerColor(this,getResources().getColor(R.color.white));
//        setNumberPickerTextColor(this,getResources().getColor(R.color.color_orange));
//        setNumberpickTextColor(this);
    }

    /**
     * 设置TimePicker的属性 颜色 大小
     * @param view
     */
    public void setNumberPicker(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextColor(this.getResources().getColor(R.color.color_blank4));
            ((EditText) view).setTextSize(16);
        }
    }
    /**
     * 设置NumberPicker字色
     *
     * @param numberPicker：NumberPicker
     * @param color：int
     * @return boolean
     */
    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                Field selectorWheelPaintField;
                try {
                    selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    try {
                        ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public EditText setNumberpickTextColor(NumberPicker numberPicker){
        try {
            Field field=numberPicker.getClass().getDeclaredField("mInputText");
           field.setAccessible(true);
            ((EditText)field.get(numberPicker)).setTextSize(30);
            ((EditText)field.get(numberPicker)).setTextColor(Color.parseColor("#FF860E"));
            return (EditText)field.get(numberPicker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void setValue(int value) {
        super.setValue(value);

    }

    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * 设置分割线的颜色值
     * @param numberPicker
     */
    @SuppressWarnings("unused")
    public void setNumberPickerDividerColor(NumberPicker numberPicker,int color) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                  pf.set(picker, new ColorDrawable(color));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                    } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
