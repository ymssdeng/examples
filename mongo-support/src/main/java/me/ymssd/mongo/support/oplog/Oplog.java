package me.ymssd.mongo.support.oplog;

import com.mongodb.BasicDBObject;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.BSONTimestamp;

/**
 * mongo oplog line
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oplog {
    private OplogOperation op;
    private BSONTimestamp ts;
    private String ns;
    private BasicDBObject o;
    private BasicDBObject o2;

    public String parseObjectId() {
        if (OplogOperation.Insert == op || OplogOperation.Delete == op) {
            return Optional.ofNullable(o.getObjectId("_id")).map(s -> s.toString()).orElse(null);
        } else if (OplogOperation.Update == op && o2 != null) {
            return Optional.ofNullable(o2.getObjectId("_id")).map(s -> s.toString()).orElse(null);
        }
        return null;
    }
}
