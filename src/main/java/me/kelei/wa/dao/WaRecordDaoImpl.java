package me.kelei.wa.dao;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.utils.WaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * 考勤记录DAO实现
 * Created by kelei on 2016/9/26.
 */
@Repository
public class WaRecordDaoImpl implements IWaRecordDao{


    @Autowired
    @Qualifier("mongoDBTemplate")
    private MongoOperations operations;

    public void saveRecordList(List<WaRecord> recordList){
        if(recordList != null && !recordList.isEmpty())
            operations.insert(recordList, WaRecord.class);
    }

    public List<WaRecord> queryRecordListByMonth(String queryDate){
        List<WaRecord> recordList = operations.find(query(where("waDate").gte(WaUtil.getStartDateOfMonth(queryDate)).
                lte(WaUtil.getEndDateOfMonth(queryDate))), WaRecord.class);
        return recordList;
    }

}
