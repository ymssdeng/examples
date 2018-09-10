package me.forward.dts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author denghui
 * @create 2018/9/6
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DtsApplication.class, args);
    }

}
