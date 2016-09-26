package me.kelei.wa.entities;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * 记录更新
 * Created by kelei on 2016/9/26.
 */
public class WaUpdate {

    //考勤号
    private String waPid;

    //最后更新时间
    @JSONField(format="yyyy-MM-dd")
    private Date lastUpdateDate;

    //更新状态，0：正在更新，1：已完成
    private String updateState;

    public String getWaPid() {
        return waPid;
    }

    public void setWaPid(String waPid) {
        this.waPid = waPid;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getUpdateState() {
        return updateState;
    }

    public void setUpdateState(String updateState) {
        this.updateState = updateState;
    }
}
