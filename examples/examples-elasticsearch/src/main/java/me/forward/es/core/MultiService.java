package me.forward.es.core;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author denghui
 * @create 2018/8/24
 */
@Service
public class MultiService {

    @Autowired
    private TransportClient client;
    @Autowired
    private NewsService newsService;
    @Autowired
    private PlayerService playerService;

    public long countByName(String name) {
        return client.prepareSearch(newsService.getIndex(), playerService.getIndex())
            .setQuery(new BoolQueryBuilder()
                .should(QueryBuilders.matchQuery("title", name))
                .should(QueryBuilders.matchQuery("name", name)))
            .get().getHits().totalHits();
    }
}
