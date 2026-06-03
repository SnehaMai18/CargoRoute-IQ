package com.example.demo.controller;

import com.example.demo.dto.VehicleDTO;
import com.example.demo.entity.enums.VehicleStatus;
import com.example.demo.entity.enums.VehicleType;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private ObjectMapper objectMapper;
    private VehicleDTO vehicleDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // ✅ REQUIRED

        vehicleDTO = new VehicleDTO();
        vehicleDTO.setVehicleID(1L);
        vehicleDTO.setRegNumber("TN01AB1234");
        vehicleDTO.setType(VehicleType.TRUCK);
        vehicleDTO.setStatus(VehicleStatus.ACTIVE);
        vehicleDTO.setMaxWeightKg(2000.0);
        vehicleDTO.setMaxVolumeM3(25.0);
        vehicleDTO.setDriverID(10L);
        vehicleDTO.setLastMaintenanceAt(LocalDateTime.now());
    }

    // ── POST /cargoRoute/vehicles/createNewVehicle ──────────────────────────

    @Test
    void createVehicle_ShouldReturn201_WhenCreated() throws Exception {
        when(vehicleService.createVehicle(any(VehicleDTO.class)))
                .thenReturn(vehicleDTO);

        mockMvc.perform(post("/cargoRoute/vehicles/createNewVehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Vehicle is succesfully created"));
    }

    // ── GET /cargoRoute/vehicles/getAllVehicles ─────────────────────────────

    @Test
    void getAllVehicles_ShouldReturn200_WithList() throws Exception {
        when(vehicleService.getAllVehicles())
                .thenReturn(List.of(vehicleDTO));

        mockMvc.perform(get("/cargoRoute/vehicles/getAllVehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regNumber").value("TN01AB1234"));
    }

    // ── GET /cargoRoute/vehicles/getVehicle/{id} ────────────────────────────

    @Test
    void getVehicleById_ShouldReturn200_WhenFound() throws Exception {
        when(vehicleService.getVehicleById(1L))
                .thenReturn(vehicleDTO);

        mockMvc.perform(get("/cargoRoute/vehicles/getVehicle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleID").value(1))
                .andExpect(jsonPath("$.regNumber").value("TN01AB1234"));
    }

    @Test
    void getVehicleById_ShouldReturn404_WhenNotFound() throws Exception {
        when(vehicleService.getVehicleById(99L))
                .thenThrow(new ResourceNotFoundException("Vehicle not found"));

        mockMvc.perform(get("/cargoRoute/vehicles/getVehicle/99"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /cargoRoute/vehicles/updateVehicle/{id} ────────────────────────

    @Test
    void updateVehicle_ShouldReturn200_WhenUpdated() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(VehicleDTO.class)))
                .thenReturn(vehicleDTO);

        mockMvc.perform(put("/cargoRoute/vehicles/updateVehicle/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Vehicle with ID: 1 is successfully updated."))
                .andExpect(jsonPath("$.updatedVehicle.vehicleID").value(1));
    }

    // ── DELETE /cargoRoute/vehicles/deleteVehicle/{id} ─────────────────────

    @Test
    void deleteVehicle_ShouldReturn200_WhenDeleted() throws Exception {
        mockMvc.perform(delete("/cargoRoute/vehicles/deleteVehicle/1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Vehicle with ID: 1 is successfully deleted."));
    }
}