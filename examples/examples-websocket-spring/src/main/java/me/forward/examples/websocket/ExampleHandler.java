package me.forward.examples.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author denghui
 * @create 2018/4/8
 */
@Slf4j
public class ExampleHandler extends TextWebSocketHandler {

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info(message.getPayload());
    }
}
