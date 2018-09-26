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
public class TopicExample {

    @Value("${example.rabbitmq.host}")
    private String host;
    @Value("${example.rabbitmq.exchange.topic}")
    private String exchange;
    @Value("${example.rabbitmq.queue.d}")
    private String queueD;
    @Value("${example.rabbitmq.queue.e}")
    private String queueE;

    @Component
    public class Pub {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "topic");
            channel.queueDeclare(queueD, false, false, false, null);
            channel.queueDeclare(queueE, false, false, false, null);

            //* one word, # one or more words
            channel.queueBind(queueD, exchange, "*.red");
            channel.queueBind(queueE, exchange, "*.yellow");

            channel.basicPublish(exchange, "car.red", null, "hello red".getBytes("UTF-8"));
            channel.basicPublish(exchange, "bike.yellow", null, "hello yellow".getBytes("UTF-8"));

            channel.close();
            connection.close();
        }
    }

    @Component
    public class SubD {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "topic");
            channel.queueDeclare(queueD, false, false, false, null);

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] D Received '" + message + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queueD, false, consumer);
        }
    }

    @Component
    public class SubE {

        @PostConstruct
        public void init() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "topic");
            channel.queueDeclare(queueE, false, false, false, null);

            com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] E Received '" + message + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            channel.basicConsume(queueE, false, consumer);
        }
    }
}
