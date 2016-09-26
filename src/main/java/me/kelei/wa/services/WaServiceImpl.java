package me.kelei.wa.services;

import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.redis.dao.IWaUpdateDao;
import me.kelei.wa.redis.dao.IWaUserDao;
import me.kelei.wa.utils.JYWaUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;

/**
 *
 * Created by kelei on 2016/9/20.
 */
@Service
public class WaServiceImpl implements IWaService {

    @Autowired
    private IWaUserDao waUserDao;

    @Autowired
    private IWaUpdateDao waUpdateDao;

    public WaUser login(String pid, String password) {
        WaUser waUser = waUserDao.getWaUser(pid);
        if(waUser == null){
            waUser = JYWaUtil.getJYWaUser(pid, password);
            if(waUser != null){

                waUserDao.saveWaUser(waUser);

                WaUpdate waUpdate = new WaUpdate();
                waUpdate.setWaPid(waUser.getWaPid());
                waUpdate.setUpdateState("1");
                try {
                    waUpdate.setLastUpdateDate(DateUtils.parseDate("2014-04-25","yyyy-MM-dd"));
                } catch (ParseException e) {
                }
                waUpdateDao.saveWaUpdate(waUpdate);
            }
        }
        return waUser;
    }

    public WaUser getWaUser(String pid){
        return waUserDao.getWaUser(pid);
    }



}
