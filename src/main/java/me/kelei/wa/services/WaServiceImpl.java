package me.kelei.wa.services;

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

import java.io.IOException;
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
        if(waUser != null)
            redisDao.saveWaUser(waUser);
        return waUser;
    }

    public void saveWaUpdate(String pid){
        WaUpdate waUpdate = new WaUpdate();
        waUpdate.setWaPid(pid);
        waUpdate.setUpdateState("1");
        try {
            waUpdate.setLastUpdateDate(DateUtils.parseDate("2014-04-25","yyyy-MM-dd"));
        } catch (ParseException e) {
            log.error("出问题我把代码吃了！", e);
        }
        redisDao.saveWaUpdate(waUpdate);
    }

    public List<WaRecord> getWaRecordList(WaUser user, String queryDate){
        return mongoDao.queryRecordListByMonth(user, queryDate);
    }

    public List<Holiday> getHolidayList(String queryDate){
        List<Holiday> holidayList = mongoDao.queryHolidayListByMonth(queryDate);
        if(holidayList != null && !holidayList.isEmpty()){
            //只需要节日名称和描述
            Map<String, String> holidayMap = new HashMap<>();
            holidayList.forEach(holiday -> holidayMap.put(holiday.getHolidayName(), holiday.getHolidayDesc()));
            holidayList.clear();
            holidayMap.forEach((k, v) ->{
                Holiday holiday = new Holiday();
                holiday.setHolidayName(k);
                holiday.setHolidayDesc(v);
                holidayList.add(holiday);
            });
        }
        return holidayList;
    }

    public void saveWaRecordList(WaUser user, String queryDate) throws IOException {
        WaUpdate update = redisDao.getWaUpdate(user.getWaPid());
        if("0".equals(update.getUpdateState())){//数据正在更新时，不在进行操作
            return;
        }

        Date queryStartDate;
        Date queryEndDate;
        boolean threadSaveFlag = false; //开启线程保存历史考勤记录
        //如果当前月的第一天大于最后更新日期 只查询当前月的考勤记录
        //否则查询当前日期到最后更新日期之间的考勤记录
        if(update.getLastUpdateDate().getTime() < WaUtil.getStartDateOfMonth(queryDate).getTime()){
            queryStartDate = WaUtil.getStartDateOfMonth(queryDate);
            queryEndDate = WaUtil.getEndDateOfMonth(queryDate);
            threadSaveFlag = true;
        }else{
            //如果查询的不是当前月的考勤记录，则不用更新数据
            if(!DateFormatUtils.format(update.getLastUpdateDate(), "yyyy-MM").equals(queryDate)){
                return;
            }
            queryStartDate = update.getLastUpdateDate();
            queryEndDate = WaUtil.getCurrentDay();
        }

        //将更新表的状态置为更新中
        update.setUpdateState("0");
        redisDao.saveWaUpdate(update);

        log.info("==============================普通保存==============================");
        saveWaRecordList(user, queryStartDate, queryEndDate);
        log.info("===================================================================");
        if(threadSaveFlag){
            new Thread(() -> {
                try {
                    log.info("==============================线程保存==============================");
                    saveWaRecordList(user, update.getLastUpdateDate(), DateUtils.addDays(queryStartDate, -1));
                    log.info("===================================================================");
                    update.setLastUpdateDate(WaUtil.getCurrentDay());
                    update.setUpdateState("1");
                    redisDao.saveWaUpdate(update);
                } catch (IOException e) {
                    log.error("线程保存考勤记录失败！", e);
                }
            }).start();
        }else{
            update.setLastUpdateDate(WaUtil.getCurrentDay());
            update.setUpdateState("1");
            redisDao.saveWaUpdate(update);
        }

    }

    private void saveWaRecordList(WaUser user, Date queryStartDate, Date queryEndDate) throws IOException {
        log.info("*************保存" + DateFormatUtils.format(queryStartDate, "yyyy-MM-dd") + "到" +
                DateFormatUtils.format(queryEndDate, "yyyy-MM-dd") + "之间的考勤记录！*************");
        //从精友考勤系统获取考勤记录
        List<WaRecord> jyRecordList = JYWaUtil.getJYWaRecordList(user, queryStartDate, queryEndDate);
        log.info("*************从精友考勤网站查到记录" + jyRecordList.size() + "条*************");
        if(!jyRecordList.isEmpty()){

            //起始查询日期的考勤记录有可能不完整，直接删除
            String updateDateStr = DateFormatUtils.format(queryStartDate, "yyyy-MM-dd");
            mongoDao.removeRecordListByDay(user, updateDateStr);

            //处理考勤数据
            jyRecordList = handleRecordList(user, jyRecordList, queryStartDate, queryEndDate);

            //将处理过的考勤数据入库
            mongoDao.saveRecordList(jyRecordList);
        }
    }

    private List<WaRecord> handleRecordList(WaUser user, List<WaRecord> recordList, Date queryStartDate, Date queryEndDate){
        List<WaRecord> handledRecordList = new ArrayList<>();

        //获取节假日列表
        List<Holiday> holidayList = getHolidayList(queryStartDate, queryEndDate);

        //将假日记录转换为map，key为日期，value为假日状态
        Map<String, String> holidayMap = WaUtil.holidayListToMap(holidayList);
        //将考勤记录按天分组，key为日期，value为当前日期的考勤列表
        Map<String, List<WaRecord>> recordMap = WaUtil.sortRecordByDay(recordList);
        //获取查询月份的所有日期列表
        List<String> dateRangeList = WaUtil.getDateRange(queryStartDate, queryEndDate);

        for(String dateStr : dateRangeList){
            try {
                Date date = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
                //如果循环日期大于当前日期，则跳出循环
                if(date.getTime() > WaUtil.getCurrentDay().getTime()){
                    return handledRecordList;
                }

                String holidayStatus = holidayMap.get(dateStr);//假期标志 1：放假，2：补休
                boolean workFlag;//工作标志  true:工作日，false:假期
                if(!StringUtils.isEmpty(holidayStatus))
                    workFlag = !"1".equals(holidayStatus);
                else
                    workFlag = !WaUtil.isWeekend(dateStr);

                //根据每天的记录数和时间来设置考勤状态
                List<WaRecord> dayRecordList = recordMap.get(dateStr);
                if(workFlag){
                    if(dayRecordList == null || dayRecordList.isEmpty()){//工作日没有记录，增加旷工记录
                        WaRecord record = new WaRecord();
                        record.setWaPid(user.getWaPid());
                        record.setWaDate(date);
                        record.setWaWeek(DateFormatUtils.format(record.getWaDate(), "EEEE", Locale.CHINA));
                        record.setWaState(WaDict.RECORD_STATE_ABSENTEEISM);
                        handledRecordList.add(record);
                    }else{
                        boolean isToday = dateStr.equals(WaUtil.getCurrentDayStr());
                        if(dayRecordList.size() == 1){
                            WaRecord record  = dayRecordList.get(0);
                            WaRecord addRecord = new WaRecord();
                            addRecord.setWaPid(user.getWaPid());
                            addRecord.setWaWeek(record.getWaWeek());
                            if(!WaUtil.isLate(record.getWaDate())){//早上没有迟到，晚上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_NORMAL);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                addRecord.setWaDate(DateUtils.parseDate(dateStr + " 18:00:00", "yyyy-MM-dd HH:mm:ss"));
                                handledRecordList.add(record);
                                if(!isToday)handledRecordList.add(addRecord);
                            }else if(!WaUtil.isEarly(record.getWaDate())){//晚上没有早退，早上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_NORMAL);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                addRecord.setWaDate(DateUtils.parseDate(dateStr + " 09:00:00", "yyyy-MM-dd HH:mm:ss"));
                                if(!isToday)handledRecordList.add(addRecord);
                                handledRecordList.add(record);
                            }else{//其它时间打卡，算迟到，晚上忘打卡
                                record.setWaState(WaDict.RECORD_STATE_LATE);
                                addRecord.setWaState(WaDict.RECORD_STATE_FORGET);
                                addRecord.setWaDate(DateUtils.parseDate(dateStr + " 18:00:00", "yyyy-MM-dd HH:mm:ss"));
                                handledRecordList.add(record);
                                if(!isToday)handledRecordList.add(addRecord);
                            }
                        }else if(dayRecordList.size() == 2){
                            setRecordState(dayRecordList.get(0), 0);
                            if(!(isToday && WaUtil.isEarly(WaUtil.getCurrentDay())))
                                setRecordState(dayRecordList.get(1), 1);
                            handledRecordList.addAll(dayRecordList);
                        }else{
                            setRecordState(dayRecordList.get(0), 0);
                            if(!(isToday && WaUtil.isEarly(WaUtil.getCurrentDay())))
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
     */
    private void setRecordState(WaRecord record, int flag) throws ParseException{
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
            record.setWaType("下班签退");
        }
    }

    /**
     * 获取假日列表
     * @param queryStartDate 起始日期
     * @param queryEndDate 结束日期
     * @return 区间节假日列表
     */
    private List<Holiday> getHolidayList(Date queryStartDate, Date queryEndDate){
        List<Holiday> holidayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(queryStartDate);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(queryEndDate);
        int endYear = calendar.get(Calendar.YEAR);
        for(int i = startYear; i <= endYear; i++){
            String year = String.valueOf(i);
            //从库中查询假日记录
            List<Holiday> yearHolidayList = mongoDao.queryHolidayListByYear(year);
            //如果没有查询到，则从API获取记录并保存到库里，然后重新查询
            if(yearHolidayList == null || yearHolidayList.isEmpty()){
                yearHolidayList = HolidayUtil.getHolidayListByYear(year);
                mongoDao.saveHolidayList(yearHolidayList);
            }
            holidayList.addAll(yearHolidayList);
        }

        return holidayList;
    }

}
