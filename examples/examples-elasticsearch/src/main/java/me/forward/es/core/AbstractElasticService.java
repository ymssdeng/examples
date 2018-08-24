package me.forward.es.core;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author denghui
 * @create 2018/8/23
 */
public abstract class AbstractElasticService<T extends ElasticDocument> {

    @Autowired
    protected TransportClient client;
    private Class<T> clazz;

    public AbstractElasticService(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract String getIndex();

    protected abstract String getType();

    public void save(T entity) {
        String source = JSON.toJSONString(entity);
        client.prepareIndex(getIndex(), getType(), entity.getId()).setSource(source).get();
    }

    public void save(List<T> entities) {
        BulkRequestBuilder builder = client.prepareBulk();
        for (T entity : entities) {
            builder.add(new IndexRequest(getIndex(), getType(), entity.getId()));
        }
        builder.get();
    }

    public T findById(String id) {
        GetResponse res = client.prepareGet(getIndex(), getType(), id).get();
        if (res.isExists() && !res.isSourceEmpty()) {
            return JSON.parseObject(res.getSourceAsString(), clazz);
        }
        return null;
    }

    public List<T> findByIds(List<String> ids) {
        List<T> list = new ArrayList<>();
        MultiGetResponse res = client.prepareMultiGet().add(getIndex(), getType(), ids).get();
        for (MultiGetItemResponse multiRes : res.getResponses()) {
            if (!multiRes.isFailed() && multiRes.getResponse().isExists() && !multiRes.getResponse().isSourceEmpty()) {
                T entity = JSON.parseObject(multiRes.getResponse().getSourceAsString(), clazz);
                list.add(entity);
            }
        }
        return list;
    }
}
