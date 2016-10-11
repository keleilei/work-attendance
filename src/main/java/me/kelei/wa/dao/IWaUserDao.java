package me.kelei.wa.dao;

import me.kelei.wa.entities.WaUser;

/**
 * 用户DAO
 * Created by kelei on 2016/9/25.
 */
public interface IWaUserDao {

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
