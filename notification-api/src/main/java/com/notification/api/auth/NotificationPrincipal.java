package com.notification.api.auth;

import java.security.Principal;
import java.util.Objects;

public class NotificationPrincipal implements Principal {
    private final String name;
    private final String authType;
    
    public NotificationPrincipal(String name, String authType) {
        this.name = name;
        this.authType = authType;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public String getAuthType() {
        return authType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationPrincipal that = (NotificationPrincipal) obj;
        return Objects.equals(name, that.name) && Objects.equals(authType, that.authType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, authType);
    }
    
    @Override
    public String toString() {
        return "NotificationPrincipal{name='" + name + "', authType='" + authType + "'}";
    }
}