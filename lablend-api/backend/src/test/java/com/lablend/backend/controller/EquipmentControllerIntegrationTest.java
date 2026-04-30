
package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.auth.dto.LoginResponse;
import com.lablend.backend.auth.dto.LoginRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; 
import com.lablend.backend.auth.filter.JwtAuthenticationFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.lablend.backend.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class EquipmentControllerIntegrationTest {

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        equipmentService.getAllEquipment().forEach(e -> 
            equipmentService.deleteEquipment(e.getId())
        );
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRemoteGetAllEquipment() {
        
        User adminUser = new User();
        adminUser.setName("admin");
        adminUser.setEmail("admin@lablend.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        
        LoginRequest loginRequest = 
            new com.lablend.backend.auth.dto.LoginRequest("admin", "admin@lablend.com", "admin");
        ResponseEntity<LoginResponse> loginResponse = 
            restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            
        String token = loginResponse.getBody().token();

        
        Equipment e1 = new Equipment();
        e1.setName("Integration Test Scope");
        e1.setType("Test Type");
        e1.setStatus(EquipmentStatus.AVAILABLE);
        equipmentService.createEquipment(e1);

       
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
            restTemplate.exchange("/api/equipment", HttpMethod.GET, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"name\":\"Integration Test Scope\""));
    }
}