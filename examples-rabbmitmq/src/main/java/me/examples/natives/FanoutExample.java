package me.examples.natives;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
public class FanoutExample {

    @Value("${example.rabbitmq.host}")
    private String host;
    @Value("${example.rabbitmq.exchange.fanout}")
    private String exchange;
    @Value("${example.rabbitmq.queue.b}")
    private String queueB;
    @Value("${example.rabbitmq.queue.c}")
    private String queueC;

    @Component
    public class Producer {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "fanout");
            channel.queueDeclare(queueB, false, false, false, null);
            channel.queueDeclare(queueC, false, false, false, null);
            //routing key任意可以
            channel.queueBind(queueB, exchange, "xx");
            channel.queueBind(queueC, exchange, "yy");
            channel.basicPublish(exchange, "zz", null, "hello".getBytes("UTF-8"));

            channel.close();
            connection.close();
        }

    }

    @Component
    public class ConsumerB {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "fanout");
            channel.queueDeclare(queueB, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] B Received '" + message + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queueB, false, consumer);
        }
    }

    @Component
    public class ConsumerC {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(queueC, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] C Received '" + message + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queueC, false, consumer);
        }
    }
}
