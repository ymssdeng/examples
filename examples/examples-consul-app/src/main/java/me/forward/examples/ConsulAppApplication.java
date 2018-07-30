package me.forward.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author denghui
 * @create 2018/7/30
 */
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class ConsulAppApplication {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping("/hello")
    public String hello() {
        return restTemplate().getForEntity("http://examples-consul-ms/hello", String.class).getBody();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsulAppApplication.class, args);
    }

}
