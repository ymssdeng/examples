package me.forward.dts;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.forward.dts.model.QuerySplit;
import me.forward.dts.model.Record;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Slf4j
@Component
public class QuerySplitRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Record> query(QuerySplit querySplit) {
        List<Record> records = new ArrayList<>();
        DBObject query = new BasicDBObject("_id",
            new BasicDBObject("$gte", new ObjectId(querySplit.getIdRange().lowerEndpoint()))
                .append("$lte", new ObjectId(querySplit.getIdRange().upperEndpoint())));
        DBCursor cursor = mongoTemplate.getCollection(querySplit.getSourceTable()).find(query);
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            Record record = new Record();
            for (String key : object.keySet()) {
                record.add(new SimpleEntry<>(key, object.get(key)));
            }
            records.add(record);
        }
        log.info("query split:{}", querySplit);
        return records;
    }
}
