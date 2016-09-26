package me.kelei.wa.services;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;

import java.util.List;

/**
 * 从精友考勤系统获取数据
 * Created by kelei on 2016/9/20.
 */
public interface IWaService {

    /**
     * 保存用户信息
     * @param userName
     * @param password
     * @return
     */
    WaUser saveUser(String userName, String password);

    /**
     * 获取用户
     * @param pid
     * @return
     */
    WaUser getWaUser(String pid);

    /**
     * 根据日期查询考勤记录
     * @param queryDate 查询日期（yyyy年MM月）
     * @return
     */
    List<WaRecord> getRecordList(String queryDate);

    /**
     * 批量保存考勤记录
     * @param recordList
     */
    void saveWaRecordList(List<WaRecord> recordList);
}
