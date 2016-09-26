package me.kelei.wa.redis.serializer;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 使用FastJson序列化
 * Created by kelei on 2016/9/26.
 */
public class FastJson2JsonRedisSerializer <T> implements RedisSerializer<T> {

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(){}

    public FastJson2JsonRedisSerializer(Class<T> clazz){
        this.clazz = clazz;
    }

    public byte[] serialize(T t) throws SerializationException {
        if(t == null){
            return new byte[0];
        }
        return JSON.toJSONBytes(t);
    }

    public T deserialize(byte[] bytes) throws SerializationException {
        if(bytes == null || bytes.length <= 0){
            return null;
        }
        return JSON.parseObject(bytes, clazz != null ? clazz : Object.class);
    }

}
