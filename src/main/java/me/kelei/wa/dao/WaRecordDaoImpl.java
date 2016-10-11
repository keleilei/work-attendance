package me.kelei.wa.dao;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.entities.WaRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

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

    public void saveWaRecord(WaRecord waRecord){
        WaRecord record = new WaRecord();
        record.setWaPid("1098");
        record.setWaDate(new Date());
        record.setWaDevice("aaaaa");
        record.setWaState("1");
        record.setWaValidateWay("bbbb");
        record.setWaWeek("星期二");
//        operations.insert(record);
//        System.out.println(record.getId());
        WaRecord qRecord = operations.findById("57fcb59e1ff6e5308c7d0447", WaRecord.class);
//        WaRecord qRecord = operations.findOne(query(where("waPid").is("1098")), WaRecord.class);
        System.out.println(JSON.toJSON(qRecord));
    }

}
