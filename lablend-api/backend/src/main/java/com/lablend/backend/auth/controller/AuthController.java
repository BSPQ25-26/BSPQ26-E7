package com.lablend.backend.auth.controller;

import com.lablend.backend.auth.dto.LoginRequest;
import com.lablend.backend.auth.dto.LoginResponse;
import com.lablend.backend.auth.service.JwtService;
import com.lablend.backend.entity.User;
import com.lablend.backend.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication, token generation, and session management.")

public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user and generate JWT token", 
        description = "Validates the user credentials (username/email and password). If valid, generates a secure JSON Web Token (JWT) containing the user's role and database ID for session authorization."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully authenticated. Returns a valid JWT bearer token."),
        @ApiResponse(responseCode = "400", description = "Bad Request: Username/principal or password payload is missing or blank."),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials or authenticated user account not found.")
    })

    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.principal() == null || request.principal().isBlank() || request.password() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.principal(), request.password()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userRepository
                    .findByName(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Authenticated user not found"));

                String token = jwtService.generateToken(
                    userDetails,
                    Map.of(
                        "role", user.getRole().name(),
                        "userId", user.getId()));
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(401).build();
        }
    }
}
