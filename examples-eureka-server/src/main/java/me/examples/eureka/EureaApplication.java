package me.examples.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author denghui
 * @create 2018/7/27
 */
@EnableEurekaServer
@SpringBootApplication
public class EureaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EureaApplication.class, args);
    }
}
