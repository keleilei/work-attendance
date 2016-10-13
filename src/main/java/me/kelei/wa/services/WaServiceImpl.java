package me.kelei.wa.services;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.dao.IWaMongoDao;
import me.kelei.wa.dao.IWaRedisDao;
import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.utils.HolidayUtil;
import me.kelei.wa.utils.JYWaUtil;
import me.kelei.wa.utils.WaDict;
import me.kelei.wa.utils.WaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 *
 * Created by kelei on 2016/9/20.
 */
@Service
public class WaServiceImpl implements IWaService {

    private static final Logger log = LoggerFactory.getLogger(WaServiceImpl.class);

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

    public List<WaRecord> saveWaRecordList(WaUser user, List<WaRecord> recordList, String queryDate){
        List<WaRecord> localRecordList = mongoDao.queryRecordListByMonth(user, queryDate);
        // 本地没有记录，则保存全部精友记录
        // 本地有记录，则比对记录，保存本地没有的记录
        if(localRecordList != null && !localRecordList.isEmpty()){
            recordList = getUnsavedRecordList(recordList, localRecordList);
        }
        if(!recordList.isEmpty()){
            //处理考勤数据
            recordList = handleRecordList(user, recordList, queryDate);
            //将处理过的考勤数据入库
            mongoDao.saveRecordList(recordList);
            //返回查询结果
            return mongoDao.queryRecordListByMonth(user, queryDate);
        }
        return localRecordList;
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

    private List<WaRecord> handleRecordList(WaUser user, List<WaRecord> recordList, String queryDate){
        List<WaRecord> handledRecordList = new ArrayList<>();
        String year = queryDate.substring(0,4);

        //从库中查询假日记录
        List<Holiday> holidayList = mongoDao.queryHolidayListByYear(year);
        //如果没有查询到，则从API获取记录并保存到库里，然后重新查询
        if(holidayList == null || holidayList.isEmpty()){
            mongoDao.saveHolidayList(HolidayUtil.getHolidayListByYear(year));
            holidayList = mongoDao.queryHolidayListByYear(year);
        }

        //将假日记录转换为map，key为日期，value为假日状态
        Map<String, String> holidayMap = WaUtil.holidayListToMap(holidayList);
        //将考勤记录按天分组，key为日期，value为当前日期的考勤列表
        Map<String, List<WaRecord>> recordMap = WaUtil.sortRecordByDay(recordList);
        //获取查询月份的所有日期列表
        List<String> dateRangeList = WaUtil.getDateRangeByMonth(queryDate);

        for(String dateStr : dateRangeList){
            try {
                Date currentDate = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
                //如果循环日期大于当前日期，则跳出循环
                if(currentDate.getTime() > new Date().getTime()){
                    return handledRecordList;
                }
                String holidayStatus = holidayMap.get(dateStr);

                boolean workFlag;//工作标志  true:工作日，false:假期
                if(!StringUtils.isEmpty(holidayStatus)){
                    workFlag = !"1".equals(holidayStatus);
                }else
                    workFlag = !WaUtil.isWeekend(dateStr);

                //根据每天的记录数和时间来设置考勤状态
                List<WaRecord> dayRecordList = recordMap.get(dateStr);
                if(workFlag){
                    if(dayRecordList == null || dayRecordList.isEmpty()){//工作日没有记录，增加旷工记录
                        WaRecord record = new WaRecord();
                        record.setWaPid(user.getWaPid());
                        record.setWaDate(currentDate);
                        record.setWaWeek(DateFormatUtils.format(record.getWaDate(), "EEEE"));
                        record.setWaState(WaDict.RECORD_STATE_ABSENTEEISM);
                        handledRecordList.add(record);
                    }else{
                        if(dayRecordList.size() == 1){
                            WaRecord record  = dayRecordList.get(0);
                            WaRecord addRecord = new WaRecord();
                            addRecord.setWaPid(user.getWaPid());
                            addRecord.setWaDate(currentDate);
                            addRecord.setWaWeek(record.getWaWeek());
                            if(!WaUtil.isLate(record.getWaDate())){//早上没有迟到，晚上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_NORMAL);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                handledRecordList.add(record);
                                handledRecordList.add(addRecord);
                            }else if(!WaUtil.isEarly(record.getWaDate())){//晚上没有早退，早上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_NORMAL);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                handledRecordList.add(addRecord);
                                handledRecordList.add(record);
                            }else{//其它时间打卡，算迟到，晚上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_LATE);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                handledRecordList.add(record);
                                handledRecordList.add(addRecord);
                            }
                        }else if(dayRecordList.size() == 2){
                            setRecordState(dayRecordList.get(0), 0);
                            setRecordState(dayRecordList.get(1), 1);
                            handledRecordList.addAll(dayRecordList);
                        }else{
                            setRecordState(dayRecordList.get(0), 0);
                            setRecordState(dayRecordList.get(dayRecordList.size() - 1), 1);
                            handledRecordList.addAll(dayRecordList);
                        }
                    }
                }else{
                    if(dayRecordList != null && !dayRecordList.isEmpty()){
                        if(dayRecordList.size() > 1){
                            dayRecordList.get(0).setWaState(WaDict.RECORD_STATE_OVERTIME);
                            dayRecordList.get(dayRecordList.size() - 1).setWaState(WaDict.RECORD_STATE_OVERTIME);
                            handledRecordList.addAll(dayRecordList);
                        }
                    }
                }
            }catch (ParseException e){
                log.error("处理考勤记录时解析日期出错！", e);
            }
        }

        return handledRecordList;
    }

    /**
     * 根据记录的时间设置记录状态
     * @param record 记录
     * @param flag 0：早上签到，1：晚上签退
     * @return
     */
    private WaRecord setRecordState(WaRecord record, int flag) throws ParseException{
        //签到
        if(flag == 0){
            if(WaUtil.isLate(record.getWaDate()))
                record.setWaState(WaDict.RECORD_STATE_LATE);
            else
                record.setWaState(WaDict.RECORD_STATE_NORMAL);
        }else{
            //签退
            if(WaUtil.isEarly(record.getWaDate()))
                record.setWaState(WaDict.RECORD_STATE_EARLY);
            else
                record.setWaState(WaDict.RECORD_STATE_NORMAL);
        }
        return record;
    }

}
