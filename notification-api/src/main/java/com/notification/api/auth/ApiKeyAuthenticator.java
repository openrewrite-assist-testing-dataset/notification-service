package com.notification.api.auth;

import io.dropwizard.auth.Authenticator;

import java.util.List;
import java.util.Optional;

public class ApiKeyAuthenticator implements Authenticator<String, NotificationPrincipal> {
    private final List<String> validApiKeys;

    public ApiKeyAuthenticator(List<String> validApiKeys) {
        this.validApiKeys = validApiKeys;
    }

    @Override
    public Optional<NotificationPrincipal> authenticate(String apiKey) {
        if (apiKey != null && validApiKeys.contains(apiKey)) {
            return Optional.of(new NotificationPrincipal("api-user", "api-key"));
        }
        return Optional.empty();
    }
}