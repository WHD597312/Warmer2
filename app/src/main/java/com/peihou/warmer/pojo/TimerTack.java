package com.peihou.warmer.pojo;

import java.io.Serializable;

public class TimerTack implements Serializable {

    private static final long serialVersionUID = 5606349432639684112L;
    private String deviceMac;//设备mac地址
    private int type;//定时类型 1 日定时 2周定时
    private int hour;//定时小时
    private int min;//定时分钟
    private int temp;//定时温度

    public TimerTack(){}

    public TimerTack(String deviceMac, int type, int hour, int min, int temp) {
        this.deviceMac = deviceMac;
        this.type = type;
        this.hour = hour;
        this.min = min;
        this.temp = temp;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
