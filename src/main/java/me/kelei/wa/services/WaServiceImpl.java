package me.kelei.wa.services;

import me.kelei.wa.dao.IWaMongoDao;
import me.kelei.wa.dao.IWaRedisDao;
import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.utils.HolidayUtil;
import me.kelei.wa.utils.JYWaUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * Created by kelei on 2016/9/20.
 */
@Service
public class WaServiceImpl implements IWaService {

    @Autowired
    private IWaRedisDao redisDao;

    @Autowired
    private IWaMongoDao mongoDao;

    public WaUser getWaUser(String pid){
        return redisDao.getWaUser(pid);
    }

    public WaUser saveUser(String pid, String password){
        WaUser waUser = JYWaUtil.getJYWaUser(pid, password);
        if(waUser != null){

            redisDao.saveWaUser(waUser);

            WaUpdate waUpdate = new WaUpdate();
            waUpdate.setWaPid(waUser.getWaPid());
            waUpdate.setUpdateState("1");
            try {
                waUpdate.setLastUpdateDate(DateUtils.parseDate("2014-04-25","yyyy-MM-dd"));
            } catch (ParseException e) {
            }
            redisDao.saveWaUpdate(waUpdate);
        }
        return waUser;
    }

    public List<WaRecord> getRecordList(String queryDate){
        return mongoDao.queryRecordListByMonth(queryDate);
    }

    public List<WaRecord> saveWaRecordList(List<WaRecord> recordList, String queryDate){
        List<WaRecord> localRecordList = mongoDao.queryRecordListByMonth(queryDate);
        // 本地没有记录，则保存全部精友记录
        // 本地有记录，则比对记录，保存本地没有的记录
        if(localRecordList != null && !localRecordList.isEmpty()){
            recordList = getUnsavedRecordList(recordList, localRecordList);
        }
        //处理考勤数据
        parseRecordList(recordList, queryDate);
        //将处理过的考勤数据入库
        mongoDao.saveRecordList(recordList);
        //返回查询结果
        return mongoDao.queryRecordListByMonth(queryDate);
    }

    /**
     * 如果本地记录和精友考勤记录的数量不同，则返回本地没有的记录
     * @return
     */
    private List<WaRecord> getUnsavedRecordList(List<WaRecord> jyRecordList, List<WaRecord> localRecordList){
        Set<String> set = new HashSet<>();
        List<WaRecord> unsavedRecordList = new ArrayList<>();
        for(WaRecord record : localRecordList){
            set.add(record.toString());
        }
        for(WaRecord record : jyRecordList){
            boolean isRepeat = set.add(record.toString());
            if(isRepeat){
                unsavedRecordList.add(record);
            }
        }
        return unsavedRecordList;
    }

    private void parseRecordList(List<WaRecord> recordList, String queryDate){
        String year = queryDate.substring(0,4);
        List<Holiday> holidayList = mongoDao.queryHolidayListByYear(year);
        if(holidayList == null || holidayList.isEmpty()){
            mongoDao.saveHolidayList(HolidayUtil.getHolidayListByYear(year));
            holidayList = mongoDao.queryHolidayListByYear(year);
        }
    }

}
