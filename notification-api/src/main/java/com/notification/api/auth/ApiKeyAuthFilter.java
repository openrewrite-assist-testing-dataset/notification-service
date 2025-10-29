package com.notification.api.auth;

import io.dropwizard.auth.AuthFilter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthFilter<P extends Principal> extends AuthFilter<String, P> {
    
    private ApiKeyAuthFilter() {}

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String apiKey = requestContext.getHeaders().getFirst("X-API-Key");
        
        if (apiKey == null) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        try {
            final Optional<P> result = authenticator.authenticate(apiKey);
            if (result.isPresent()) {
                requestContext.setSecurityContext(new ApiKeySecurityContext(result.get(), "API_KEY", requestContext));
                return;
            }
        } catch (Exception e) {
            logger.warn("Error authenticating API key", e);
        }

        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    public static class Builder<P extends Principal> extends AuthFilter.AuthFilterBuilder<String, P, ApiKeyAuthFilter<P>> {
        @Override
        protected ApiKeyAuthFilter<P> newInstance() {
            return new ApiKeyAuthFilter<>();
        }
    }
    
    private static class ApiKeySecurityContext implements SecurityContext {
        private final Object principal;
        private final String scheme;
        private final ContainerRequestContext requestContext;

        public ApiKeySecurityContext(Object principal, String scheme, ContainerRequestContext requestContext) {
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