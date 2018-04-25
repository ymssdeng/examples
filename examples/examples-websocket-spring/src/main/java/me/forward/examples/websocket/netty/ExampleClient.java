package me.forward.examples.websocket.netty;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.emitter.Emitter.Listener;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author denghui
 * @create 2018/4/17
 */
public class ExampleClient {
    private static Logger LOGGER = LoggerFactory.getLogger(ExampleClient.class);

    public static void main(String[] args) throws Exception {
        Manager manager = new Manager(new URI("http://localhost:9092"));
        final Socket socket = manager.socket("n1");

        socket.on("event", new Emitter.Listener(){
            @Override
            public void call(Object... objects) {
                LOGGER.info("id {} received msg {}", socket.id(), objects[0]);
            }
        }).on(Socket.EVENT_CONNECT, new Listener() {
            @Override
            public void call(Object... args) {
                LOGGER.info("connected to server");
            }
        });

        socket.connect();
    }

}
