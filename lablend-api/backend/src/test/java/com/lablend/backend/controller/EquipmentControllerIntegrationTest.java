package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.auth.dto.LoginResponse;
import com.lablend.backend.auth.dto.LoginRequest;
import com.lablend.backend.repository.EquipmentRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean; 
import org.springframework.http.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.http.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.lablend.backend.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private EquipmentService equipmentService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRemoteGetAllEquipment() {
        
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("admin");
        adminUser.setEmail("admin@lablend.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(UserRole.ADMIN);
        when(userRepository.findByName("admin")).thenReturn(Optional.of(adminUser));

        LoginRequest loginRequest = 
            new com.lablend.backend.auth.dto.LoginRequest("admin", "admin@lablend.com", "admin");
        ResponseEntity<LoginResponse> loginResponse = 
            restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            
        String token = loginResponse.getBody().token();

        Equipment e1 = new Equipment();
        e1.setId(10L);
        e1.setName("Integration Test Scope");
        e1.setType("Test Type");
        e1.setStatus(EquipmentStatus.AVAILABLE);
        
        when(equipmentService.getAllEquipment()).thenReturn(List.of(e1));
        
        Page<Equipment> page = new PageImpl<>(List.of(e1));
        when(equipmentService.getAllEquipmentPaged(any(Pageable.class))).thenReturn(page);

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