package me.ymssd.mongo.support.oplog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import me.ymssd.mongo.support.config.MongoSupportProperties;

/**
 * @author denghui
 * @create 2018/9/4
 */
@Slf4j
public class DefaultOplogConsumer implements OplogConsumer {

    private ExecutorService executor;

    public DefaultOplogConsumer(MongoSupportProperties properties) {
        executor = Executors.newFixedThreadPool(properties.getOplog().getConsumerCount());
    }

    @Override
    public void consume(Oplog oplog) {
        executor.submit(() -> handleOplog(oplog));
    }

    protected void handleOplog(Oplog oplog) {
        log.info("{}", oplog);
    }

    @Override
    public void close() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(200, TimeUnit.MICROSECONDS);
        } catch (InterruptedException e) { }
        log.info("consumer stopped");
    }
}
