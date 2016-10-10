import org.junit.*;
import redis.clients.jedis.Jedis;

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
        jedis.set("a","hello world!");
        String str = jedis.get("a");
        System.out.println(str);
    }

}
