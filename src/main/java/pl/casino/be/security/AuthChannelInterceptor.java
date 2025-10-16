package pl.casino.be.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final FirebaseAuth firebaseAuth;

    public AuthChannelInterceptor(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // This logic runs only for the initial CONNECT message from the client
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // "nativeHeaders" contains headers from the client's STOMP connect call
            List<String> authorization = accessor.getNativeHeader("Authorization");
            log.debug("Authorization header: {}", authorization);

            if (authorization == null || authorization.isEmpty()) {
                return message; // Or throw an exception if you want to force auth
            }

            String token = authorization.getFirst();
            if (token != null && token.startsWith("Bearer ")) {
                String tokenStr = token.substring(7);
                try {
                    // Verify the token
                    FirebaseToken decodedToken = firebaseAuth.verifyIdToken(tokenStr);
                    String uid = decodedToken.getUid();

                    // Get roles from custom claims
                    Map<String, Object> claims = decodedToken.getClaims();
                    String role = (String) claims.getOrDefault("role", "USER");
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(MessageFormat.format("ROLE_{0}", role)));

                    // Create the Spring Security Authentication object
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(uid, null, authorities);

                    // Set the user for this WebSocket session
                    accessor.setUser(authentication);
                    log.info("Authenticated WebSocket user: {}", uid);
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                }
            }
        }
        return message;
    }
}