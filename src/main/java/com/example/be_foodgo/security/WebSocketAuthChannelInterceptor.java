package com.example.be_foodgo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ID_SESSION_ATTRIBUTE = "wsUserId";

    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, String> simpSessionUsers = new ConcurrentHashMap<>();

    public WebSocketAuthChannelInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        return switch (command) {
            case CONNECT -> authenticateConnect(message, accessor);
            case SUBSCRIBE -> {
                restoreSessionPrincipal(accessor);
                log.info("[STOMP DEBUG] SUBSCRIBE principal='{}', destination='{}', sessionId='{}'",
                        principalName(accessor.getUser()),
                        accessor.getDestination(),
                        accessor.getSessionId());
                yield message;
            }
            case SEND -> {
                restoreSessionPrincipal(accessor);
                log.info("[STOMP DEBUG] SEND principal='{}', destination='{}', sessionId='{}'",
                        principalName(accessor.getUser()),
                        accessor.getDestination(),
                        accessor.getSessionId());
                yield message;
            }
            case DISCONNECT -> {
                restoreSessionPrincipal(accessor);
                log.info("[STOMP DEBUG] DISCONNECT principal='{}', sessionId='{}'",
                        principalName(accessor.getUser()),
                        accessor.getSessionId());
                removeSessionUser(accessor);
                yield message;
            }
            default -> {
                restoreSessionPrincipal(accessor);
                yield message;
            }
        };
    }

    private Message<?> authenticateConnect(Message<?> message, StompHeaderAccessor accessor) {
        String authorizationHeader = firstNativeHeader(accessor, AUTHORIZATION_HEADER);
        Principal principal = authenticate(accessor);
        if (principal == null) {
            throw new IllegalArgumentException("Khong the xac thuc ket noi websocket. Vui long su dung backend JWT hop le.");
        }

        accessor.setUser(principal);
        setSessionUser(accessor, principal.getName());
        log.info("[STOMP DEBUG] CONNECT Authorization present={}, rawPrefix={}, principal='{}', sessionId='{}'",
                StringUtils.hasText(authorizationHeader),
                maskAuthorizationPrefix(authorizationHeader),
                principal.getName(),
                accessor.getSessionId());
        log.info("[STOMP DEBUG] Driver connected → principal='{}', sessionId='{}'", principal.getName(), accessor.getSessionId());
        log.info("WebSocket CONNECT xac thuc thanh cong cho userId={}", principal.getName());
        return message;
    }

    private void restoreSessionPrincipal(StompHeaderAccessor accessor) {
        String sessionUserId = getSessionUser(accessor);
        if (StringUtils.hasText(sessionUserId)) {
            accessor.setUser(createPrincipal(sessionUserId));
        }
    }

    private Principal authenticate(StompHeaderAccessor accessor) {
        String authorizationHeader = firstNativeHeader(accessor, AUTHORIZATION_HEADER);
        String jwt = jwtTokenProvider.layTokenTuHeader(authorizationHeader);
        if (StringUtils.hasText(jwt) && jwtTokenProvider.xacThucToken(jwt)) {
            String userId = jwtTokenProvider.layUserIdTuToken(jwt);
            if (StringUtils.hasText(userId)) {
                return createPrincipal(userId);
            }
        }

        log.warn("Tu choi WebSocket CONNECT vi khong co backend JWT hop le trong header Authorization.");
        return null;
    }

    private UsernamePasswordAuthenticationToken createPrincipal(String userId) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String headerName) {
        String value = accessor.getFirstNativeHeader(headerName);
        if (StringUtils.hasText(value)) {
            return value;
        }

        List<String> fromSession = accessor.getNativeHeader(headerName);
        if (fromSession != null && !fromSession.isEmpty()) {
            return fromSession.get(0);
        }
        return null;
    }

    private String maskAuthorizationPrefix(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return "missing";
        }
        int firstSpace = authorizationHeader.indexOf(' ');
        return firstSpace > 0 ? authorizationHeader.substring(0, firstSpace) : authorizationHeader;
    }

    private void setSessionUser(StompHeaderAccessor accessor, String userId) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put(USER_ID_SESSION_ATTRIBUTE, userId);
        }

        String sessionId = accessor.getSessionId();
        if (StringUtils.hasText(sessionId)) {
            simpSessionUsers.put(sessionId, userId);
        }
    }

    private String getSessionUser(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object userId = sessionAttributes.get(USER_ID_SESSION_ATTRIBUTE);
            if (userId instanceof String value && StringUtils.hasText(value)) {
                return value;
            }
        }

        String sessionId = accessor.getSessionId();
        if (StringUtils.hasText(sessionId)) {
            return simpSessionUsers.get(sessionId);
        }
        return null;
    }

    private void removeSessionUser(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.remove(USER_ID_SESSION_ATTRIBUTE);
        }

        String sessionId = accessor.getSessionId();
        if (StringUtils.hasText(sessionId)) {
            simpSessionUsers.remove(sessionId);
        }
    }

    private String principalName(Principal principal) {
        return principal != null ? principal.getName() : "anonymous";
    }
}
