package me.forward.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author denghui
 * @create 2018/7/27
 */
@SpringBootApplication
@EnableEurekaClient
@RestController
public class EurekaAppApplication {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping("/hello")
    public String hello() {
        return restTemplate().getForEntity("http://examples-eureka-ms/hello", String.class).getBody();
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaAppApplication.class, args);
    }

}
