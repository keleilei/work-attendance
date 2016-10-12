package me.kelei.wa.entities;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

/**
 * 考勤记录
 * Created by kelei on 2016/9/24.
 */
public class WaRecord implements Serializable{

    private String id;

    //考勤号
    private String waPid;

    //考勤日期
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date waDate;

    //星期
    private String waWeek;

    //考勤类型
    private String waType;

    //验证方式
    private String waValidateWay;

    //考勤设备
    private String waDevice;

    //考勤状态 0:无效记录，1：正常，2：迟到，3：早退，4：旷工，5：假期加班
    private String waState;

    @Override
    public String toString(){
        return waPid+waDate.getTime();
    }

    public Date getWaDate() {
        return waDate;
    }

    public void setWaDate(Date waDate) {
        this.waDate = waDate;
    }

    public String getWaType() {
        return waType;
    }

    public void setWaType(String waType) {
        this.waType = waType;
    }

    public String getWaValidateWay() {
        return waValidateWay;
    }

    public void setWaValidateWay(String waValidateWay) {
        this.waValidateWay = waValidateWay;
    }

    public String getWaDevice() {
        return waDevice;
    }

    public void setWaDevice(String waDevice) {
        this.waDevice = waDevice;
    }

    public String getWaPid() {
        return waPid;
    }

    public void setWaPid(String waPid) {
        this.waPid = waPid;
    }

    public String getWaState() {
        return waState;
    }

    public void setWaState(String waState) {
        this.waState = waState;
    }

    public String getWaWeek() {
        return waWeek;
    }

    public void setWaWeek(String waWeek) {
        this.waWeek = waWeek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
