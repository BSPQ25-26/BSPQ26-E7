package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;
import com.lablend.backend.entity.EquipmentStatus; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(EquipmentController.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentService equipmentService;

    @Test
    void testGetAllEquipment() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);

        when(equipmentService.getAllEquipment()).thenReturn(List.of(equipment));

        mockMvc.perform(get("/api/equipment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Microscope"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
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

@org.springframework.boot.test.context.SpringBootTest(
    webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class EquipmentControllerIntegrationTest {

    @Autowired
    private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    @Autowired
    private EquipmentService equipmentService; 

    @Test
    void testRemoteGetAllEquipment() {
        // 1. Setup real data in the database
        Equipment e1 = new Equipment();
        e1.setName("Integration Test Scope");
        e1.setType("Test Type");
        e1.setStatus(EquipmentStatus.AVAILABLE);
        equipmentService.createEquipment(e1);

        // 2. Perform the remote call
        org.springframework.http.ResponseEntity<Equipment[]> response = 
            restTemplate.getForEntity("/api/equipment", Equipment[].class);

        // 3. Verify real response from the database through the server
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        org.junit.jupiter.api.Assertions.assertTrue(response.getBody().length > 0);
        org.junit.jupiter.api.Assertions.assertEquals("Integration Test Scope", response.getBody()[0].getName());
    }
}