package me.forward.dts;

import com.google.common.collect.Range;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.forward.dts.config.DtsProperties;
import me.forward.dts.model.Record;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Slf4j
@ConditionalOnClass(MongoTemplate.class)
@Component
public class MongoQuerySplitRunner implements QuerySplitRunner {

    @Autowired
    private DtsProperties dtsProperties;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Range<String> getMinMaxId() {
        String collection = dtsProperties.getQuery().getTable();
        String minId = dtsProperties.getQuery().getMinId();
        String maxId = dtsProperties.getQuery().getMaxId();
        if (StringUtils.isEmpty(minId)) {
            DBObject query = new BasicDBObject();
            DBObject projection = new BasicDBObject("_id", 1);
            DBObject sort = new BasicDBObject("_id", 1);
            DBObject min = mongoTemplate.getCollection(collection).findOne(query, projection, sort);
            minId = min.get("_id").toString();
        }
        if (StringUtils.isEmpty(maxId)) {
            DBObject query = new BasicDBObject();
            DBObject projection = new BasicDBObject("_id", 1);
            DBObject sort = new BasicDBObject("_id", -1);
            DBObject max = mongoTemplate.getCollection(collection).findOne(query, projection, sort);
            maxId = max.get("_id").toString();
        }

        return Range.closed(minId, maxId);
    }

    @Override
    public List<String> splitId(Range<String> range) {
        int step = dtsProperties.getQuery().getStep();
        int lowerTime = Integer.valueOf(range.lowerEndpoint().substring(0, 8), 16);
        int upperTime = Integer.valueOf(range.upperEndpoint().substring(0, 8), 16);
        String suffix = range.lowerEndpoint().substring(8);
        List<String> ids = new ArrayList<>();
        ids.add(range.lowerEndpoint());
        while (lowerTime + step < upperTime) {
            lowerTime += step;
            ids.add(Integer.toHexString(lowerTime) + suffix);
        }
        ids.add(range.upperEndpoint());
        return ids;
    }

    public List<Record> query(Range<String> range) {
        List<Record> records = new ArrayList<>();
        DBObject query = new BasicDBObject("_id",
            new BasicDBObject("$gte", new ObjectId(range.lowerEndpoint()))
                .append("$lte", new ObjectId(range.upperEndpoint())));
        DBCursor cursor = mongoTemplate.getCollection(dtsProperties.getQuery().getTable()).find(query);
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            Record record = new Record();
            for (String key : object.keySet()) {
                record.add(new SimpleEntry<>(key, object.get(key)));
            }
            records.add(record);
        }
        log.info("query split:{}", range);
        return records;
    }
}
