package me.ymssd.mongo.support.oplog;

/**
 * @author denghui
 * @create 2018/9/4
 */
public interface OplogConsumer {

    void consume(Oplog oplog);

    void close();
}
