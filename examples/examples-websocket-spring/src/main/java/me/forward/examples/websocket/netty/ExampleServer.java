package me.forward.examples.websocket.netty;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author denghui
 * @create 2018/4/17
 */
public class ExampleServer {

    private static Logger LOGGER = LoggerFactory.getLogger(ExampleServer.class);

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();

        config.setHostname("localhost");
        config.setPort(9092);
        final SocketIOServer server = new SocketIOServer(config);

        final SocketIONamespace n1 = server.addNamespace("n1");

        n1.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                LOGGER.info("one more connect");
                client.sendEvent("event", "hello");
            }
        });
        server.start();

    }

    public static class Message {
        private String value;

        public Message() {
        }

        public Message(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
