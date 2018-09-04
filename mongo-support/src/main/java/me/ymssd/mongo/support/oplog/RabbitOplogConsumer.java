package me.ymssd.mongo.support.oplog;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import me.ymssd.mongo.support.config.MongoSupportProperties;
import me.ymssd.mongo.support.config.MongoSupportProperties.OplogProperties;

/**
 * @author denghui
 * @create 2018/9/4
 */
@Slf4j
public class RabbitOplogConsumer implements OplogConsumer {

    private MongoSupportProperties properties;
    private Channel channel;

    public RabbitOplogConsumer(MongoSupportProperties properties) throws IOException, TimeoutException {
        this.properties = properties;

        OplogProperties oplogProperties = properties.getOplog();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(oplogProperties.getRabbitHost());
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(oplogProperties.getRabbitExchange(), "direct");
        channel.queueDeclare(oplogProperties.getRabbitQueue(), false, false, false, null);
        channel.queueBind(oplogProperties.getRabbitQueue(), oplogProperties.getRabbitExchange(), oplogProperties.getRabbitRouting());
    }

    @Override
    public void consume(Oplog oplog) {
        RabbitOplog rabbitOplog = new RabbitOplog(oplog.getOp().getCode(),
            oplog.getTs().getTime(),
            oplog.getTs().getInc(),
            oplog.getNs(),
            oplog.getO().toJson(),
            oplog.getO2().toJson());
        OplogProperties oplogProperties = properties.getOplog();
        String json = JSON.toJSONString(rabbitOplog);
        try {
            channel.basicPublish(oplogProperties.getRabbitExchange(), oplogProperties.getRabbitRouting(), null,
                json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("fail publish:{}", oplog);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) { }
    }
}
