package com.lablend.backend.auth.dto;

/**
 * Login request payload for authentication.
 */
public record LoginRequest(String username, String email, String password) {

	public String principal() {
		if (username != null && !username.isBlank()) {
			return username;
		}
		return email;
	}
}
