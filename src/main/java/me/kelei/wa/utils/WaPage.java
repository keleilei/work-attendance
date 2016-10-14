package me.kelei.wa.utils;

import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;

import java.util.List;

/**
 * 回传页面数据
 * Created by kelei on 2016/10/14.
 */
public class WaPage {

    private String status;

    private List<WaRecord> recordList;

    private List<Holiday> holidayList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<WaRecord> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<WaRecord> recordList) {
        this.recordList = recordList;
    }

    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    public void setHolidayList(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }
}
