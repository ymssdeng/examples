package me.ymssd.mongo.support.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/4
 */
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "mongo.support")
public class MongoSupportProperties {

    private OplogProperties oplog;

    @Data
    @NoArgsConstructor
    public static class OplogProperties {
        private int consumerCount = 1;
        private String rabbitHost;
        private String rabbitExchange;
        private String rabbitQueue;
        private String rabbitRouting;
    }
}
