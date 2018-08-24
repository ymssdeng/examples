package me.forward.es.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author denghui
 * @create 2018/8/24
 */
@Service
@Slf4j
public class NewsService extends AbstractElasticService<News> {

    public NewsService() {
        super(News.class);
    }

    @Override
    protected String getIndex() {
        return "news";
    }

    @Override
    protected String getType() {
        return "news";
    }

}
