package me.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author denghui
 * @create 2018/7/27
 */
@RestController
@SpringBootApplication
@EnableEurekaClient
@Slf4j
public class EurekaMsApplication {

    @RequestMapping("/hello")
    public String hello() {
        log.info("hello");
        return "hello eureka";
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaMsApplication.class, args);
    }

}
