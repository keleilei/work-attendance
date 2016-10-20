import org.junit.*;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * redis测试类
 * Created by kelei on 2016/9/22.
 */
public class RedisTest {

    private Jedis jedis;

    @Before
    public void setup(){
        jedis = new Jedis("localhost");
        jedis.auth("kelei");
    }

    @Test
    public void testString(){
        Set<String> strs = jedis.keys("waupdate:*");
        strs.forEach(str -> {
            System.out.println(jedis.get(str));
        });
    }

    @Test
    public void removeRedisData(){
        Set<String> keys = jedis.keys("wauser:*");
        keys.forEach(key -> jedis.del(key));
        keys = jedis.keys("waupdate:*");
        keys.forEach(key -> jedis.del(key));
    }

    @After
    public void close(){
        jedis.close();
    }

}
