package me.forward.dts;

import static java.time.Instant.*;
import static java.time.LocalDateTime.ofInstant;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/11
 */
@Component
@Slf4j
public class MetricService {

    private static final Metric METRIC = new Metric();
    private static final DateTimeFormatter YMDHMS= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public void startQuery() {
        METRIC.setQueryStartTime(System.currentTimeMillis());
    }

    public void endQuery() {
        METRIC.setQueryEndTime(System.currentTimeMillis());
    }

    public void addSize(int delta) {
        METRIC.getSize().addAndGet(delta);
    }

    public void startSink() {
        if (METRIC.getSinkStartTime() == 0) {
            METRIC.setSinkStartTime(System.currentTimeMillis());
        }
    }

    public void endSink() {
        METRIC.setSinkEndTime(System.currentTimeMillis());
    }

    public void addSankSize(int delta) {
        METRIC.getSankSize().addAndGet(delta);
    }

    public void reset() {
        METRIC.setQueryStartTime(0);
        METRIC.setQueryEndTime(0);
        METRIC.getSize().set(0);
        METRIC.setSinkStartTime(0);
        METRIC.setSinkEndTime(0);
        METRIC.getSankSize().set(0);
    }

    public void print() {
        ZoneId zoneId = ZoneId.systemDefault();
        log.info("-->queryStartTime:{}", YMDHMS.format(ofInstant(ofEpochMilli(METRIC.getQueryStartTime()), zoneId)));
        log.info("-->queryEndTime:{}", YMDHMS.format(ofInstant(ofEpochMilli(METRIC.getQueryEndTime()), zoneId)));
        log.info("-->size:{}", METRIC.getSize());
        log.info("-->sinkStartTime:{}", YMDHMS.format(ofInstant(ofEpochMilli(METRIC.getSinkStartTime()), zoneId)));
        log.info("-->sinkEndTime:{}", YMDHMS.format(ofInstant(ofEpochMilli(METRIC.getSinkEndTime()), zoneId)));
        log.info("-->sankSize:{}", METRIC.getSankSize());
    }

    @Data
    private static class Metric {

        private long queryStartTime;
        private long queryEndTime;
        private AtomicLong size = new AtomicLong(0);

        private long sinkStartTime;
        private long sinkEndTime;
        private AtomicLong sankSize = new AtomicLong(0);
    }
}
