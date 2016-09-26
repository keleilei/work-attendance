package me.kelei.wa.redis.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.utils.RedisSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 记录更新DAO实现
 * Created by kelei on 2016/9/26.
 */
@Repository
public class WaUpdateDaoImpl implements IWaUpdateDao{

    @Autowired
    private RedisTemplate<String, JSONObject> template;

    public WaUpdate getWaUpdate(String pid){
        JSONObject object = template.opsForValue().get(RedisSchema.SCHEMA_UPDATE + pid);
        WaUpdate waUpdate = object.toJavaObject(WaUpdate.class);
        return waUpdate;
    }

    public void saveWaUpdate(WaUpdate update){
        template.opsForValue().set(RedisSchema.SCHEMA_UPDATE + update.getWaPid(),
                JSONObject.parseObject(JSON.toJSONString(update)));
    }
}
