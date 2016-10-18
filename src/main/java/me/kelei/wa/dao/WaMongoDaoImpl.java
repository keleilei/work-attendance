package me.kelei.wa.dao;

import com.mongodb.WriteResult;
import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.utils.WaUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * 考勤记录DAO实现
 * Created by kelei on 2016/9/26.
 */
@Repository
public class WaMongoDaoImpl implements IWaMongoDao {

    private static final Logger log = LoggerFactory.getLogger(WaMongoDaoImpl.class);

    @Autowired
    @Qualifier("mongoDBTemplate")
    private MongoOperations operations;

    public void saveRecordList(List<WaRecord> recordList){
        if(recordList != null && !recordList.isEmpty())
            operations.insert(recordList, WaRecord.class);
    }

    public List<WaRecord> queryRecordListByMonth(WaUser user, String queryDate){
        return operations.find(query(where("waDate").
                gte(WaUtil.getStartDateOfMonth(queryDate)).
                lte(WaUtil.getEndDateOfMonth(queryDate)).
                and("waPid").is(user.getWaPid())), WaRecord.class);
    }

    public void removeRecordListByDay(WaUser user, String queryDate){
        try {
            operations.remove(query(where("waDate").
                    gte(DateUtils.parseDate(queryDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss")).
                    lte(DateUtils.parseDate(queryDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss")).
                    and("waPid").is(user.getWaPid())), WaRecord.class);
        } catch (ParseException e) {
            log.error("删除考勤记录时解析日期失败！", e);
        }
    }

    public void saveHolidayList(List<Holiday> holidayList){
        if(holidayList != null && !holidayList.isEmpty())
            operations.insert(holidayList, Holiday.class);
    }

    public List<Holiday> queryHolidayListByYear(String year){
        return operations.find(query(where("year").is(year)), Holiday.class);
    }

    public List<Holiday> queryHolidayListByMonth(String month){
        return operations.find(query(where("holidayDate").regex("^" + month)), Holiday.class);
    }

}
