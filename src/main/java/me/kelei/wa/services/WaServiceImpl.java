package me.kelei.wa.services;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.dao.IWaRecordDao;
import me.kelei.wa.dao.IWaUpdateDao;
import me.kelei.wa.dao.IWaUserDao;
import me.kelei.wa.utils.JYWaUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private IWaRecordDao waRecordDao;

    public WaUser getWaUser(String pid){
        return waUserDao.getWaUser(pid);
    }

    public WaUser saveUser(String pid, String password){
        WaUser waUser = JYWaUtil.getJYWaUser(pid, password);
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
        return waUser;
    }

    public List<WaRecord> getRecordList(String queryDate){


        return null;
    }

    public void saveWaRecordList(List<WaRecord> recordList){
        WaRecord record = new WaRecord();
        record.setWaPid("1098");
        record.setWaDevice("aaaaaaaaa");
        record.setWaDate(new Date());
        record.setWaType("bbb");
        record.setWaValidateWay("cccccc");
        record.setWaState("ddddddddddd");
        waRecordDao.saveWaRecord(record);
    }

}
