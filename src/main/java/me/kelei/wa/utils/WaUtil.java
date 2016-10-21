package me.kelei.wa.utils;

import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工具类
 * Created by kelei on 2016/10/11.
 */
public class WaUtil {

    private static final Logger logger = LoggerFactory.getLogger(WaUtil.class);

    /**
     * 获取月份第一天
     * @param month yyyy-MM
     * @return
     */
    public static Date getStartDateOfMonth(String month){
        String startDate = month + "-01";
        Date date = null;
        try {
            date = DateUtils.parseDate(startDate, "yyyy-MM-dd");
        } catch (ParseException e) {
            logger.error("解析日期失败：" + startDate);
        }
        return date;
    }

    /**
     * 获取月份第一天
     * @param month yyyy-MM
     * @return
     */
    public static String getStartDateOfMonthStr(String month){
        String startDate = month + "-01";
        return startDate;
    }

    /**
     * 获取月份最后一天
     * @param month yyyy-MM
     * @return
     */
    public static Date getEndDateOfMonth(String month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartDateOfMonth(month));
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        return calendar.getTime();
    }

    /**
     * 获取月份最后一天
     * @param month yyyy-MM
     * @return
     */
    public static String getEndDateOfMonthStr(String month){
        return DateFormatUtils.format(getEndDateOfMonth(month), "yyyy-MM-dd");
    }

    /**
     * 将假日列表转换为map
     * @param holidayList
     * @return
     */
    public static Map<String, String> holidayListToMap(List<Holiday> holidayList){
        Map<String, String> holidayMap = new HashMap<>();
        if(holidayList != null && !holidayList.isEmpty()){
            for(Holiday holiday : holidayList){
                holidayMap.put(holiday.getHolidayDate(), holiday.getHolidayStatus());
            }
        }
        return holidayMap;
    }

    /**
     * 获取给定月份的日期列表
     * @param month yyyy-MM
     * @return
     * @throws Exception
     */
    public static List<String> getDateRangeByMonth(String month){
        return getDateRange(getStartDateOfMonth(month), getEndDateOfMonth(month));
    }

    /**
     * 获取给定两个日期之前的区间日期列表
     * @param fromDate
     * @param toDate
     * @return
     * @throws Exception
     */
    public static List<String> getDateRange(Date fromDate, Date toDate){
        List<String> rangeList = new ArrayList<String>();
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTime(fromDate);
        while(gc.getTime().getTime() <= toDate.getTime()){
            String tmpDate = DateFormatUtils.format(gc.getTime(), "yyyy-MM-dd");
            rangeList.add(tmpDate);
            gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }
        return rangeList;
    }

    /**
     * 按天给记录归类
     * @return
     */
    public static Map<String, List<WaRecord>> sortRecordByDay(List<WaRecord> recordList){
        Map<String, List<WaRecord>> recordMap = new HashMap<>();
        if(recordList != null && !recordList.isEmpty()){
            int size = recordList.size();
            for(int i = 0; i < size; i++){
                //如果已经归类完成，列表为空，跳出循环
                if(recordList.isEmpty()) break;
                //每次获取列表的第一个日期，然后循环列表，找到相同日期的记录，存入同一天的列表中，然后把它从大列表中删除
                String date = DateFormatUtils.format(recordList.get(0).getWaDate(), "yyyy-MM-dd");
                List<WaRecord> dayRecordList = new ArrayList<>();
                recordMap.put(date, dayRecordList);
                for(int j = 0; j < recordList.size(); j++){
                    WaRecord record = recordList.get(j);
                    String waDate = DateFormatUtils.format(record.getWaDate(), "yyyy-MM-dd");
                    if(date.equals(waDate)){
                        dayRecordList.add(record);
                        recordList.remove(j);
                        j--;
                    }
                }
            }
        }
        return recordMap;
    }

    /**
     * 判断是否是周末
     * @param date
     * @return
     */
    public static boolean isWeekend(String date) throws ParseException {
        String week = DateFormatUtils.format(DateUtils.parseDate(date, "yyyy-MM-dd"),"EEEE", Locale.CHINA);
        return week.equals("星期六") || week.equals("星期日");
    }

    /**
     * 判断是否迟到
     * @param date
     * @return
     */
    public static boolean isLate(Date date) throws ParseException {
        String standardDate = DateFormatUtils.format(date, "yyyy-MM-dd") + " 09:06:00";
        return date.getTime() > DateUtils.parseDate(standardDate, "yyyy-MM-dd HH:mm:ss").getTime();
    }

    /**
     * 判断是否早退
     * @param date
     * @return
     */
    public static boolean isEarly(Date date) throws ParseException {
        String standardDate = DateFormatUtils.format(date, "yyyy-MM-dd") + " 18:00:00";
        return date.getTime() < DateUtils.parseDate(standardDate, "yyyy-MM-dd HH:mm:ss").getTime();
    }

    /**
     * 判断是否是下午
     * @param date
     * @return
     */
    public static boolean isAfternoon(Date date) throws ParseException {
        String standardDate = DateFormatUtils.format(date, "yyyy-MM-dd") + " 13:00:00";
        return date.getTime() > DateUtils.parseDate(standardDate, "yyyy-MM-dd HH:mm:ss").getTime();
    }

    /**
     * 返回当前日期字符串
     * @return
     */
    public static String getCurrentDayStr(){
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    }

    /**
     * 返回当前日期字符串
     * @return
     */
    public static Date getCurrentDay(){
        return new Date();
    }

}
