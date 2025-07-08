package com.notification.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.dropwizard.auth.Authenticator;

import java.util.Optional;

public class JwtAuthenticator implements Authenticator<String, NotificationPrincipal> {
    private final JWTVerifier verifier;

    public JwtAuthenticator(String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    @Override
    public Optional<NotificationPrincipal> authenticate(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            String subject = jwt.getSubject();
            
            // Check if token has required claims
            String scope = jwt.getClaim("scope").asString();
            if (subject != null && scope != null && 
                (scope.contains("notification:read") || scope.contains("notification:write"))) {
                return Optional.of(new NotificationPrincipal(subject, "jwt"));
            }
        } catch (JWTVerificationException e) {
            // Token verification failed
        }
        return Optional.empty();
    }
}