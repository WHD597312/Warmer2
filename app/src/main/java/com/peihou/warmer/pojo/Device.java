package com.peihou.warmer.pojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Device {
    @Id(autoincrement = true)
    private Long id;
    private String deviceName;
    private String deviceMac;
    private int deviceId;
    @Generated(hash = 2147174802)
    public Device(Long id, String deviceName, String deviceMac, int deviceId) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
        this.deviceId = deviceId;
    }
    @Generated(hash = 1469582394)
    public Device() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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
}
