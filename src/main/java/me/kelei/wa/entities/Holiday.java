package me.kelei.wa.entities;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * 节假日
 * Created by kelei on 2016/10/12.
 */
public class Holiday {

    //年份
    private String year;

    //节假日名称
    private String holidayName;

    //日期
    private String holidayDate;

    //节假日描述
    private String holidayDesc;

    //节假日状态 1：放假，2：补休
    private String holidayStatus;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getHolidayDesc() {
        return holidayDesc;
    }

    public void setHolidayDesc(String holidayDesc) {
        this.holidayDesc = holidayDesc;
    }

    public String getHolidayStatus() {
        return holidayStatus;
    }

    public void setHolidayStatus(String holidayStatus) {
        this.holidayStatus = holidayStatus;
    }
}
