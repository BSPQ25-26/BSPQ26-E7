package com.lablend.backend.auth.dto;

/**
 * Login response payload containing JWT token.
 */
public record LoginResponse(String token) {
}
