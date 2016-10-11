package me.kelei.wa.dao;

import me.kelei.wa.entities.WaRecord;

import java.util.List;

/**
 * 考勤记录DAO
 * Created by kelei on 2016/9/26.
 */
public interface IWaRecordDao {

    /**
     * 保存考勤记录
     * @param recordList
     */
    void saveRecordList(List<WaRecord> recordList);

    /**
     * 根据月份查询考勤记录
     * @param queryDate
     * @return
     */
    List<WaRecord> queryRecordListByMonth(String queryDate);

}
