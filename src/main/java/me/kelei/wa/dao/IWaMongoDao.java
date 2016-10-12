package me.kelei.wa.dao;

import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;

import java.util.List;

/**
 * 考勤记录DAO
 * Created by kelei on 2016/9/26.
 */
public interface IWaMongoDao {

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

    /**
     * 保存节假日
     * @param holidayList
     */
    void saveHolidayList(List<Holiday> holidayList);

    /**
     * 根据年份查询节假日
     * @param year
     * @return
     */
    List<Holiday> queryHolidayListByYear(String year);

}
