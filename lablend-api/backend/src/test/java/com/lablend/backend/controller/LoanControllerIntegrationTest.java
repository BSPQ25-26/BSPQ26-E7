package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
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
class LoanControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        loanRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRemoteCreateAndGetLoan() {
        User adminUser = new User();
        adminUser.setName("Admin Loan");
        adminUser.setEmail("admin.loan@lablend.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(UserRole.ADMIN);
        User savedAdmin = userRepository.save(adminUser);

        Equipment equipment = new Equipment();
        equipment.setName("Telescope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        LoginRequest loginRequest = new LoginRequest("Admin Loan", "admin.loan@lablend.com", "admin");
        ResponseEntity<LoginResponse> loginResponse = 
            restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = loginResponse.getBody().token();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String newLoanJson = String.format("""
            {
                "userId": %d,
                "equipmentId": %d
            }
            """, savedAdmin.getId(), savedEquipment.getId());
            
        HttpEntity<String> createRequestEntity = new HttpEntity<>(newLoanJson, headers);

        ResponseEntity<Loan> createResponse =
            restTemplate.exchange("/api/loans", HttpMethod.POST, createRequestEntity, Loan.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        Long newLoanId = createResponse.getBody().getId();
        HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<Loan> getResponse =
            restTemplate.exchange("/api/loans/" + newLoanId, HttpMethod.GET, getRequestEntity, Loan.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(savedAdmin.getId(), getResponse.getBody().getUserId());
        assertEquals(savedEquipment.getId(), getResponse.getBody().getEquipmentId());
        
        Equipment updatedEquipment = equipmentRepository.findById(savedEquipment.getId()).orElseThrow();
        assertEquals(EquipmentStatus.RESERVED, updatedEquipment.getStatus());
    }
}
