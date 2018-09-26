package me.examples.spring;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/3/26
 */
@Component
public class DirectExample {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${example.rabbitmq.exchange.direct}")
    private String exchange;
    @Value("${example.rabbitmq.queue.a}")
    private String queue;
    @Value("${example.rabbitmq.queue.a.routing}")
    private String routing;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlation, boolean ack, String cause) {
                System.out.println("Received " + (ack ? " ack " : " nack ") + "for correlation: " + correlation);
            }
        });

        rabbitTemplate.convertAndSend(exchange, routing, "hello");
    }

    @RabbitListener(queues = "queue-a")
    public void listen(String in, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        channel.basicAck(tag, false);
        System.out.println("Listener received: " + in);
    }
}
