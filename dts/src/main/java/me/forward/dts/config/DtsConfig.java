package me.forward.dts.config;

import com.mongodb.MongoClientURI;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.UnknownHostException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Configuration
public class DtsConfig {

    @Autowired
    private DtsProperties dtsProperties;

    @Bean
    public MongoDbFactory mongoDbFactory() throws UnknownHostException {
        return new SimpleMongoDbFactory(new MongoClientURI(dtsProperties.getQuery().getUrl()));
    }

    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        return new MongoTemplate(mongoDbFactory());
    }

    @Bean
    public HikariDataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dtsProperties.getSink().getUrl());
        config.setUsername(dtsProperties.getSink().getUsername());
        config.setPassword(dtsProperties.getSink().getPassword());
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(mysqlDataSource());
    }
}
