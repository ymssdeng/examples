package me.examples.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author denghui
 * @create 2018/8/23
 */
@SpringBootApplication
@Configuration
@RestController
public class ElasticApplication {

    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication.run(ElasticApplication.class, args);
    }

    @Bean
    public TransportClient transportClient() throws UnknownHostException {
        TransportClient client = TransportClient.builder().build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
        return client;
    }

}
