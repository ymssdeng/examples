package me.forward.examples.natives;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/3/23
 */
@Component
public class DirectExample {

    @Value("${example.rabbitmq.host}")
    private String host;
    @Value("${example.rabbitmq.exchange.direct}")
    private String exchange;
    @Value("${example.rabbitmq.queue.a}")
    private String queue;
    @Value("${example.rabbitmq.queue.a.routing}")
    private String routing;

    @Component
    public class Producer {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct");
            channel.queueDeclare(queue, false, false, false, null);
            channel.queueBind(queue, exchange, routing);

            channel.basicPublish(exchange, routing, null, "hello".getBytes("UTF-8"));

            channel.close();
            connection.close();
        }

    }

    @Component
    public class Consumer {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(queue, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queue, false, consumer);
        }
    }
}
