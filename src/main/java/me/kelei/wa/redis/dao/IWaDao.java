package me.kelei.wa.redis.dao;

import me.kelei.wa.entities.WaUser;

/**
 *
 * Created by kelei on 2016/9/25.
 */
public interface IWaDao {

    /**
     * 获取用户
     * @param wapid
     * @return
     */
    WaUser getWaUser(String wapid);

    /**
     * 保存用户
     * @param user
     */
    void saveWaUser(WaUser user);
}
