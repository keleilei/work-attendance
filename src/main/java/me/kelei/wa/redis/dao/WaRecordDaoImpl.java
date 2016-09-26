package me.kelei.wa.redis.dao;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.utils.RedisSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 考勤记录DAO实现
 * Created by kelei on 2016/9/26.
 */
@Repository
public class WaRecordDaoImpl implements IWaRecordDao{


    @Autowired
    private RedisTemplate<String, WaRecord> template;

    private BeanUtilsHashMapper<WaRecord> mapper = new BeanUtilsHashMapper<>(WaRecord.class);

    public void saveWaRecord(WaRecord waRecord){
        Map<String, String> map = mapper.toHash(waRecord);
        System.out.println(map.get("waDate"));
        template.opsForHash().put(RedisSchema.SCHEMA_RECORD, waRecord.getWaPid(), map);
    }

}
