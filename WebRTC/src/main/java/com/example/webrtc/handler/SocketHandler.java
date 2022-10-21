package com.example.webrtc.handler;

import com.example.webrtc.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SocketHandler extends TextWebSocketHandler {

    ObjectMapper objectMapper = new ObjectMapper();
    List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    /**
     * 메세지를 전송 받았을때 호출
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("you got user's message");
        String payload = message.getPayload();
        log.info("session id {}, user message {}",session.getId(),payload);
        log.info("session protocol {}", session.getAcceptedProtocol());
        log.info("session local Address {}", session.getLocalAddress());
        log.info("session Remote Address {}", session.getRemoteAddress());
        log.info("session URI {}", session.getUri());

        MessageDto messageDto = new MessageDto();
        messageDto.setMessage(payload);
        messageDto.setName(message.toString());

        String json = objectMapper.writeValueAsString(messageDto);

        for (WebSocketSession webSocketSession : sessions) {
            // 자신을 제외한 다른 유저들에게
            if (webSocketSession.isOpen() && !(session.getId().equals(webSocketSession.getId()))){
                webSocketSession.sendMessage(new TextMessage(json));
            }
        }
    }

    /**
     * web socket 연결후 호출
     * html 렌더링 되면서 js 호출되고 이를 통해 socket 연결되면서 호출
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("user entered...");
        log.info("session id {}", session.getId());
        log.info("session protocol {}", session.getAcceptedProtocol());
        log.info("session local Address {}", session.getLocalAddress());
        log.info("session Remote Address {}", session.getRemoteAddress());
        log.info("session URI {}", session.getUri());
        sessions.add(session);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("user left...");
        log.info("session id {}", session.getId());
        log.info("session protocol {}", session.getAcceptedProtocol());
        log.info("session local Address {}", session.getLocalAddress());
        log.info("session Remote Address {}", session.getRemoteAddress());
        log.info("session URI {}", session.getUri());
        sessions.remove(session);
    }
}
