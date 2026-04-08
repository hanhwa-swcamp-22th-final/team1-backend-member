package com.conk.member.command.controller;

import org.springframework.security.core.Authentication;

final class CommandControllerSupport {
    private CommandControllerSupport() {
    }

    static String resolveAccountId(Authentication authentication, String fallbackAccountId) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return fallbackAccountId;
    }

    static String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
