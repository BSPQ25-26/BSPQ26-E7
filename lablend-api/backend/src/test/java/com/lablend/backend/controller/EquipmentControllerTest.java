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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}