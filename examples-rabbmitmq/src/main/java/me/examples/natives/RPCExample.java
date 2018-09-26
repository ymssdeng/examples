package me.examples.natives;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/3/26
 */
@Component
public class RPCExample {

    @Value("${example.rabbitmq.host}")
    private String host;
    @Value("${example.rabbitmq.exchange.rpc}")
    private String exchange;
    @Value("${example.rabbitmq.queue.rpc}")
    private String queue;
    @Value("${example.rabbitmq.queue.rpc.routing}")
    private String routing;

    @Component("rpcServer")
    public class RPCServer {

        @PostConstruct
        public void init() {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);

            Connection connection = null;
            try {
                connection = factory.newConnection();
                final Channel channel = connection.createChannel();

                channel.exchangeDeclare(exchange, "direct");
                channel.queueDeclare(queue, false, false, false, null);
                channel.queueBind(queue, exchange, routing);

                channel.basicQos(1);

                System.out.println(" [x] Awaiting RPC requests");

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();

                        String response = "";

                        try {
                            String message = new String(body,"UTF-8");
                            int n = Integer.parseInt(message);

                            System.out.println(" [.] fib(" + message + ")");
                            response += fib(n);
                        }
                        catch (RuntimeException e){
                            System.out.println(" [.] " + e.toString());
                        }
                        finally {
                            channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }
                    }
                };

                channel.basicConsume(queue, false, consumer);

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }

            System.out.println("server ready");
        }

        private int fib(int n) {
            if (n ==0) return 0;
            if (n == 1) return 1;
            return fib(n-1) + fib(n-2);
        }
    }

    @Component
    @DependsOn({"rpcServer"})
    public class RPCClientA {

        private Connection connection;
        private Channel channel;
        private String replyQueueName;

        @PostConstruct
        public void init () throws Exception {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);

            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct");
            channel.queueBind(queue, exchange, routing);
            replyQueueName = channel.queueDeclare().getQueue();

            String n = "15";
            System.out.println("call fib");
            String res = call(n);
            System.out.println("fib("+n+")="+res);
        }

        public String call(String message) throws IOException, InterruptedException {
            final String corrId = UUID.randomUUID().toString();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

            channel.basicPublish(exchange, routing, props, message.getBytes("UTF-8"));

            final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });

            return response.take();
        }
    }

}
