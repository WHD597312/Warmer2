package com.peihou.warmer.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 *
 */
@Entity
public class Device implements Serializable {

    private static final long serialVersionUID = -5780650666496068977L;
    @Id(autoincrement = true)
    private Long id;
    private boolean online;
    private String deviceMac;
    private int deviceId;
    private String deviceName;
    private int userId;
    private int mode;
    private int open;
    private int lock;

    private int setTemp;
    private int currentTemp;
    private int error;

    private int state;

    @Generated(hash = 1469582394)
    public Device() {
    }
    @Generated(hash = 1486174886)
    public Device(Long id, boolean online, String deviceMac, int deviceId,
            String deviceName, int userId, int mode, int open, int lock,
            int setTemp, int currentTemp, int error, int state) {
        this.id = id;
        this.online = online;
        this.deviceMac = deviceMac;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userId = userId;
        this.mode = mode;
        this.open = open;
        this.lock = lock;
        this.setTemp = setTemp;
        this.currentTemp = currentTemp;
        this.error = error;
        this.state = state;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceMac() {
        return this.deviceMac;
    }
    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }
    public int getDeviceId() {
        return this.deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(int setTemp) {
        this.setTemp = setTemp;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(int currentTemp) {
        this.currentTemp = currentTemp;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }
    public int getOpen() {
        return this.open;
    }
    public void setOpen(int open) {
        this.open = open;
    }
    public int getLock() {
        return this.lock;
    }
    public void setLock(int lock) {
        this.lock = lock;
    }
    public boolean getOnline() {
        return this.online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public int getUserId() {
        return this.userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
