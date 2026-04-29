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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentService equipmentService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetAllEquipment() throws Exception {

        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);

        Page<Equipment> page = new PageImpl<>(List.of(equipment));

        when(equipmentService.getAllEquipmentPaged(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/equipment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Microscope"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
    }

    @Test
    void testGetEquipmentByIdSuccess() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Microscope");
        when(equipmentService.getEquipmentById(1L)).thenReturn(java.util.Optional.of(equipment));

        mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Microscope"));
    }

    @Test
    void testGetEquipmentByIdNotFound() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateEquipment() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("Oscilloscope");
        
        when(equipmentService.createEquipment(any(Equipment.class))).thenReturn(equipment);

        mockMvc.perform(post("/api/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Oscilloscope\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oscilloscope"));
    }

    @Test
    void testUpdateEquipmentSuccess() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("Updated Microscope");

        when(equipmentService.updateEquipment(any(Long.class), any(Equipment.class))).thenReturn(equipment);

        mockMvc.perform(put("/api/equipment/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Microscope\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Microscope"));
    }

    @Test
    void testUpdateEquipmentNotFound() throws Exception {
        when(equipmentService.updateEquipment(any(Long.class), any(Equipment.class))).thenReturn(null);

        mockMvc.perform(put("/api/equipment/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Microscope\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEquipmentSuccess() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(java.util.Optional.of(new Equipment()));

        mockMvc.perform(delete("/api/equipment/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEquipmentNotFound() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/equipment/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testReserveEquipmentSuccess() throws Exception {
        Equipment reservedEquipment = new Equipment("Microscope", "Optical", EquipmentStatus.RESERVED);
        reservedEquipment.setId(1L);

        when(equipmentService.reserveEquipment(1L)).thenReturn(reservedEquipment);

        mockMvc.perform(put("/api/equipment/1/reserve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void testReserveEquipmentFail_IllegalState() throws Exception {
        when(equipmentService.reserveEquipment(1L))
                .thenThrow(new IllegalStateException("Solo se puede reservar equipo disponible."));

        mockMvc.perform(put("/api/equipment/1/reserve"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Solo se puede reservar equipo disponible."));
    }

    @Test
    void testReserveEquipmentFail_NotFound() throws Exception {
        when(equipmentService.reserveEquipment(1L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/api/equipment/1/reserve"))
                .andExpect(status().isNotFound());
    }
}

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

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRemoteGetAllEquipment() {
        // Create an admin user for authentication
        User adminUser = new User();
        adminUser.setName("admin");
        adminUser.setEmail("admin@lablend.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        // Authenticate as the admin
        LoginRequest loginRequest = 
            new com.lablend.backend.auth.dto.LoginRequest("admin", "admin@lablend.com", "admin");
        ResponseEntity<LoginResponse> loginResponse = 
            restTemplate.postForEntity("/api/auth/login", loginRequest, LoginResponse.class);
            
        String token = loginResponse.getBody().token();

        // Create the test equipment
        Equipment e1 = new Equipment();
        e1.setName("Integration Test Scope");
        e1.setType("Test Type");
        e1.setStatus(EquipmentStatus.AVAILABLE);
        equipmentService.createEquipment(e1);

        // Perform the authenticated request
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