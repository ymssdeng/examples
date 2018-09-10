package me.forward.dts.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Component
@ConfigurationProperties("dts")
@Data
@NoArgsConstructor
public class DtsProperties {

    private QueryProperties query;
    private SinkProperties sink;

    @Data
    @NoArgsConstructor
    public static class QueryProperties {
        private String mongoUrl;
        private String jdbcUrl;
        private String table;
        private String minId;
        private String maxId;
        private int step = 10000;
        private int threadCount = 5;
    }

    @Data
    @NoArgsConstructor
    public static class SinkProperties {
        private String url;
        private String username;
        private String password;
        private String table;
        private int threadCount = 5;
        private int batchSize = 100;
    }

}
