package me.kelei.wa.services;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;

import java.io.IOException;
import java.net.ConnectException;
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
     * 获取用户更新信息
     * @param pid
     * @return
     */
    void saveWaUpdate(String pid);

    /**
     * 根据月份查询考勤记录
     * @param user
     * @param queryDate
     * @return
     */
    public List<WaRecord> getWaRecordList(WaUser user, String queryDate);

    /**
     * 批量保存考勤记录
     */
    void saveWaRecordList(WaUser user, String queryDate) throws IOException;
}
