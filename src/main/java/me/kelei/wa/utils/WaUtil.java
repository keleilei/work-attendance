package me.kelei.wa.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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

}
