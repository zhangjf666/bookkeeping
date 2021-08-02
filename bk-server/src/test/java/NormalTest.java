import com.hc.bookkeeping.BookkeepingServerApplication;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.common.utils.JsonUtil;
import com.hc.bookkeeping.modules.bkeeping.entity.AccountBook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = BookkeepingServerApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class NormalTest {

    @Test
    public void JsonTest(){
        AccountBook ab = new AccountBook();
        ab.setName("默认账本");
        ab.setImage("red");
        ab.setUserId(11111L);
        ab.setDescription("默认账本");
        ab.setIsDefault(BoolEnum.True);
        System.out.println(JsonUtil.toJsonString(ab));
    }
}
