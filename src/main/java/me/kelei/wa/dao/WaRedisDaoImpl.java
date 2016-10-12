package me.kelei.wa.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kelei.wa.entities.WaUpdate;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.utils.RedisSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * RedisDAO实现
 * Created by kelei on 2016/9/25.
 */
@Repository
public class WaRedisDaoImpl implements IWaRedisDao {

    @Autowired
    private RedisTemplate<String, JSONObject> template;

    public WaUser getWaUser(String wapid){
        JSONObject object = template.opsForValue().get(RedisSchema.SCHEMA_USER + wapid);
        WaUser waUser = null;
        if(object != null){
            waUser = object.toJavaObject(WaUser.class);
        }
        return waUser;
    }

    public void saveWaUser(WaUser user){
        template.opsForValue().set(RedisSchema.SCHEMA_USER + user.getWaPid(),
                JSONObject.parseObject(JSON.toJSONString(user)));
    }

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
