package me.kelei.wa.redis.dao;

import me.kelei.wa.entities.WaUpdate;

/**
 * 记录更新DAO
 * Created by kelei on 2016/9/26.
 */
public interface IWaUpdateDao {

    /**
     * 获取更新记录时间
     * @param pid
     * @return
     */
    WaUpdate getWaUpdate(String pid);

    /**
     * 保存更新记录时间
     * @param update
     */
    void saveWaUpdate(WaUpdate update);

}
