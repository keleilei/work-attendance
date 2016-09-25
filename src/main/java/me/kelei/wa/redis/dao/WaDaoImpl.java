package me.kelei.wa.redis.dao;

import me.kelei.wa.entities.WaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * Created by kelei on 2016/9/25.
 */
@Repository
public class WaDaoImpl implements IWaDao {
    private static String SCHEMA_USER = "wauser:";

    @Autowired
    private RedisTemplate<String, WaUser> template;

    public WaUser getWaUser(String wapid){
        WaUser user = template.opsForValue().get(SCHEMA_USER + wapid);
        return user;
    }

    public void saveWaUser(WaUser user){
        template.opsForValue().set(SCHEMA_USER + user.getWaPid(), user);
    }
}
