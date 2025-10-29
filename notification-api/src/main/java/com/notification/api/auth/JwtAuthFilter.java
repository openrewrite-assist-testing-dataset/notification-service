package com.notification.api.auth;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter<P extends Principal> extends AuthFilter<String, P> {
    
    private JwtAuthFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String header = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (header == null) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        final String method = header.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        final String token = header.substring(space + 1);
        
        try {
            final Optional<P> result = authenticator.authenticate(token);
            if (result.isPresent()) {
                requestContext.setSecurityContext(new JwtSecurityContext(result.get(), "JWT", requestContext));
                return;
            }
        } catch (Exception e) {
            logger.warn("Error authenticating credentials", e);
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    public static class Builder<P extends Principal> extends AuthFilter.AuthFilterBuilder<String, P, JwtAuthFilter<P>> {
        @Override
        protected JwtAuthFilter<P> newInstance() {
            return new JwtAuthFilter<>();
        }
    }
    
    private static class JwtSecurityContext implements SecurityContext {
        private final Object principal;
        private final String scheme;
        private final ContainerRequestContext requestContext;

        public JwtSecurityContext(Object principal, String scheme, ContainerRequestContext requestContext) {
            this.principal = principal;
            this.scheme = scheme;
            this.requestContext = requestContext;
        }

        @Override
        public Principal getUserPrincipal() {
            return (Principal) principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return true;
        }

        @Override
        public boolean isSecure() {
            return requestContext.getSecurityContext().isSecure();
        }

        @Override
        public String getAuthenticationScheme() {
            return scheme;
        }
    }
}