package me.kelei.wa.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kelei.wa.entities.Holiday;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * 获取节假日列表
 * Created by kelei on 2016/10/12.
 */
public class HolidayUtil {

    private static final Logger logger = LoggerFactory.getLogger(HolidayUtil.class);


    /**
     * 获取传入年份的法定假日列表
     * @param year
     * @return
     */
    public static List<Holiday> getHolidayListByYear(String year){
        List<Holiday> holidayList = new ArrayList<>();
        if(StringUtils.isEmpty(year)){
            year = DateFormatUtils.format(new Date(), "yyyy");
        }
        year += "年";
        for(int i = 1; i < 11; i++){
            String ym = year + i + "月";
            holidayList.addAll(getHolidayListByMonth(ym));
        }
        return removeRepeat(holidayList);
    }

    /**
     * 获取传入月的法定假日列表
     * @param month
     * @return
     */
    public static List<Holiday> getHolidayListByMonth(String month){
        List<Holiday> holidayList = new ArrayList<>();
        try{
            URL url = new URL("https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?co=&resource_id=6018&oe=utf8&format=json&t=1473170707027&query=" + month);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            String responseStr = ConvertToString(inputStream);
            urlConnection.disconnect();
            JSONObject jsonObject = JSON.parseObject(responseStr);
            Object object = jsonObject.getJSONArray("data").getJSONObject(0).get("holiday");
            if(object instanceof JSONArray){
                JSONArray array = (JSONArray) object;
                for(int i = 0; i < array.size(); i++){
                    JSONObject holidayObject = array.getJSONObject(i);
                    getHolidayList(holidayList, holidayObject);
                }
            }else{
                JSONObject holidayObject = (JSONObject) object;
                getHolidayList(holidayList, holidayObject);
            }

        }catch(Exception e){
            logger.error("获取节日API失败", e);
        }
        return holidayList;
    }

    private static String ConvertToString(InputStream inputStream) throws Exception{
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder result = new StringBuilder();
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            inputStreamReader.close();
            inputStream.close();
            bufferedReader.close();
        }
        return result.toString();
    }

    /**
     * 将JSON对象转换为实体对象
     * @param holidayList
     * @param holidayObject
     * @throws ParseException
     */
    private static void getHolidayList(List<Holiday> holidayList, JSONObject holidayObject) throws ParseException {
        String holidayName = holidayObject.getString("name");
        if("除夕".equals(holidayName)){
            return;
        }
        String holidayDesc = holidayObject.getString("desc");
        JSONArray holidayArray = holidayObject.getJSONArray("list");
        for(int j = 0; j < holidayArray.size(); j++){
            JSONObject day = holidayArray.getJSONObject(j);
            Holiday holiday = new Holiday();
            holiday.setHolidayName(holidayName);
            holiday.setHolidayDesc(holidayDesc);
            holiday.setHolidayStatus(day.getString("status"));
            holiday.setHolidayDate(DateUtils.parseDate(day.getString("date"),"yyyy-M-d"));
            holiday.setYear(DateFormatUtils.format(holiday.getHolidayDate(), "yyyy"));
            holidayList.add(holiday);
        }
    }

    /**
     * 去除重复的节假日
     * @param holidayList
     * @return
     */
    private static List<Holiday> removeRepeat(List<Holiday> holidayList){
        List<Holiday> newList = new ArrayList<>();
        Set<Date> set = new HashSet<>();
        for(Holiday holiday : holidayList){
            boolean isRepeat = set.add(holiday.getHolidayDate());
            if(isRepeat){
                newList.add(holiday);
            }
        }
        return newList;
    }

}
