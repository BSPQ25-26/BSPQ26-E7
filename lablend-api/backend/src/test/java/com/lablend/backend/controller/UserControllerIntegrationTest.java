package com.lablend.backend.controller;

import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.auth.dto.LoginRequest;
import com.lablend.backend.auth.dto.LoginResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRemoteCreateAndGetUser() {
       
        User adminUser = new User();
        adminUser.setName("admin");
        adminUser.setEmail("admin@lablend.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        
        LoginRequest loginRequest = new LoginRequest("admin", "admin@lablend.com", "admin");
        ResponseEntity<LoginResponse> loginResponse = 
            restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode(), "Login should be ok");
        String token = loginResponse.getBody().token();

        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String newUserJson = """
            {
                "name": "Maddi Test",
                "email": "maddi.test@uni.com",
                "password": "password123",
                "role": "USER"
            }
            """;
            
        HttpEntity<String> createRequestEntity = new HttpEntity<>(newUserJson, headers);

        ResponseEntity<User> createResponse =
            restTemplate.exchange("/api/users", HttpMethod.POST, createRequestEntity, User.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "It should creat the user");
        assertNotNull(createResponse.getBody());
        assertEquals("Maddi Test", createResponse.getBody().getName());
        Long newUserId = createResponse.getBody().getId();

        
        HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> getResponse =
            restTemplate.exchange("/api/users/" + newUserId, HttpMethod.GET, getRequestEntity, String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("\"email\":\"maddi.test@uni.com\""));
    }
}
