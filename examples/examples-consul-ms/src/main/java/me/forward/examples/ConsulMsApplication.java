package me.forward.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author denghui
 * @create 2018/7/30
 */
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ConsulMsApplication {

    @RequestMapping("/hello")
    public String hello() {
        return "hello consul";
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsulMsApplication.class, args);
    }

}
