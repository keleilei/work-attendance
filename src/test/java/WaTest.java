import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.utils.WaUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.util.Date;

/**
 * Created by kelei on 2016/10/11.
 */
public class WaTest {

    @Test
    public void test() throws InterruptedException {
        System.out.println(DateFormatUtils.format(new Date(), "EEEE"));
    }
}
