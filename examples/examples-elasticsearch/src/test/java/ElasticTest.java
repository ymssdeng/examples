import java.util.Arrays;
import me.forward.es.ElasticApplication;
import me.forward.es.core.MultiService;
import me.forward.es.core.News;
import me.forward.es.core.NewsService;
import me.forward.es.core.Player;
import me.forward.es.core.PlayerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author denghui
 * @create 2018/8/6
 */
@SpringBootTest(classes = { ElasticApplication.class})
@RunWith(SpringRunner.class)
public class ElasticTest {

    @Autowired
    private NewsService newsService;

    @Test
    public void news() {
        News news = new News();
        news.setId("1");
        news.setTitle("你好");
        newsService.save(news);

        System.out.println(newsService.findById("1"));

        System.out.println(newsService.findByIds(Arrays.asList("1", "2")));

        news = new News();
        news.setId("2");
        news.setTitle("哈哈哈");
        newsService.save(news);

        System.out.println(newsService.findByIds(Arrays.asList("1", "2")));
    }

    @Autowired
    private MultiService multiService;
    @Autowired
    private PlayerService playerService;
    @Test
    public void multi() {
        News news = new News();
        news.setId("100");
        news.setTitle("你好詹姆斯，哈发简历");
        newsService.save(news);
        Player player = new Player();
        player.setId("891");
        player.setName("詹姆斯-哈登");
        playerService.save(player);
        System.out.println(multiService.countByName("詹姆斯"));
    }
}
