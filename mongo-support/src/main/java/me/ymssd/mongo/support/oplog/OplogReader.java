package me.ymssd.mongo.support.oplog;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoInterruptedException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.BSONTimestamp;

/**
 * Mongo oplog reader
 */
@Slf4j
public class OplogReader {

    private MongoClient mongoClient;
    private OplogConsumer oplogConsumer;

    private BSONTimestamp ts;
    private String ns;

    private volatile boolean running;
    private ExecutorService executor;

    public OplogReader(MongoClient mongoClient, OplogConsumer oplogConsumer) {
        this.mongoClient = mongoClient;
        this.oplogConsumer = oplogConsumer;

        executor = Executors.newSingleThreadExecutor();
    }

    public void start(String ns) {
        start(null, ns);
    }

    public void start(BSONTimestamp ts, String ns) {
        if (running) {
            log.info("reader has already started");
            return;
        }
        this.ts = ts;
        this.ns = ns;
        executor.submit(new Reader());
        running = true;
    }

    public void stop() throws InterruptedException {
        running = false;
        executor.shutdownNow();
        executor.awaitTermination(200, TimeUnit.MICROSECONDS);
        oplogConsumer.close();
    }

    private class Reader implements Runnable {

        @Override
        public void run() {
            DB local = mongoClient.getDB("local");
            DBCollection collection = local.getCollection("oplog.rs");

            if (ts == null) {
                DBCursor lastCursor = collection.find().sort(new BasicDBObject("$natural", -1)).limit(1);
                if (!lastCursor.hasNext()) {
                    log.error("no oplog configured for this connection.");
                    return;
                }
                DBObject last = lastCursor.next();
                ts = (BSONTimestamp) last.get("ts");
            }
            log.info("starting point: " + ts);

            try {
                while (running) {
                    BasicDBObject q = new BasicDBObject("ts", new BasicDBObject("$gt", ts));
                    q.append("ns", ns);
                    log.info("oplog query: {}", q);

                    DBCursor cursor = collection.find(q);
                    cursor.addOption(Bytes.QUERYOPTION_TAILABLE);
                    cursor.addOption(Bytes.QUERYOPTION_AWAITDATA);

                    while (running && cursor.hasNext()) {
                        DBObject x = cursor.next();
                        ts = (BSONTimestamp) x.get("ts");
                        Oplog oplog = parseOpLog(ts, x);

                        oplogConsumer.consume(oplog);
                    }
                    log.info("exit cursor loop");
                }
                log.info("exit reader loop");
            } catch (MongoInterruptedException e) {
                log.warn("oplog reader is interrupted");
            } catch (Exception e) {
                log.error("fail in oplog reader", e);
            }
        }
    }

    protected Oplog parseOpLog(BSONTimestamp ts, DBObject x) {
        OplogOperation operation = OplogOperation.find((String) x.get("op"));
        String nameSpace = (String) x.get("ns");
        BasicDBObject data = (BasicDBObject) x.get("o");
        BasicDBObject o2 = (BasicDBObject) x.get("o2");
        return new Oplog(operation, ts, nameSpace, data, o2);
    }

}
