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

    private String sourceUrl;
    private String sourceTable;
    private int queryThread = 5;

    private String targetUrl;
    private String username;
    private String password;
    private String targetTable;
    private int sinkThread = 5;
    private int batchSize = 100;

}
