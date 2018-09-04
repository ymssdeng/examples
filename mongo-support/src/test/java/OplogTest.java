import com.mongodb.MongoClient;
import java.util.concurrent.TimeUnit;
import me.ymssd.mongo.support.MongoSupportTestApplication;
import me.ymssd.mongo.support.config.MongoSupportProperties;
import me.ymssd.mongo.support.oplog.DefaultOplogConsumer;
import me.ymssd.mongo.support.oplog.OplogConsumer;
import me.ymssd.mongo.support.oplog.OplogReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author denghui
 * @create 2018/9/4
 */
@SpringBootTest(classes = {MongoSupportTestApplication.class})
@RunWith(SpringRunner.class)
public class OplogTest {

    @Autowired
    private MongoSupportProperties properties;

    @Bean
    public MongoClient mongoClient() {
        return new MongoClient("10.1.126.11:20099");
    }

    @Bean
    public OplogConsumer oplogConsumer() {
        return new DefaultOplogConsumer(properties);
    }

    @Bean
    public OplogReader oplogReader() {
        return new OplogReader(mongoClient(), oplogConsumer());
    }

    @Test
    public void test() throws InterruptedException {
        OplogReader reader = oplogReader();
        reader.start("fenqifu.geinihua");

        TimeUnit.SECONDS.sleep(15);

        reader.stop();

        TimeUnit.SECONDS.sleep(15);
    }
}
