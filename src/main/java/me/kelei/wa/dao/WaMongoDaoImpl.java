package me.kelei.wa.dao;

import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.utils.WaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * 考勤记录DAO实现
 * Created by kelei on 2016/9/26.
 */
@Repository
public class WaMongoDaoImpl implements IWaMongoDao {


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

    public void saveHolidayList(List<Holiday> holidayList){
        if(holidayList != null && !holidayList.isEmpty())
            operations.insert(holidayList, Holiday.class);
    }

    public List<Holiday> queryHolidayListByYear(String year){
        return operations.find(query(where("year").is(year)), Holiday.class);
    }

}
