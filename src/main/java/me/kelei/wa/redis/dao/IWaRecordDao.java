package me.kelei.wa.redis.dao;

import me.kelei.wa.entities.WaRecord;

/**
 * 考勤记录DAO
 * Created by kelei on 2016/9/26.
 */
public interface IWaRecordDao {

    /**
     * 保存考勤记录
     * @param waRecord
     */
    void saveWaRecord(WaRecord waRecord);

}
