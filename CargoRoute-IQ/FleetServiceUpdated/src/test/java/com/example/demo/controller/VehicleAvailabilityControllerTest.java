package com.example.demo.controller;

import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleAvailability;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.VehicleAvailabilityService;
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
class VehicleAvailabilityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleAvailabilityService service;

    @InjectMocks
    private VehicleAvailabilityController controller;

    private ObjectMapper objectMapper;
    private VehicleAvailability availability;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleID(10L);

        availability = new VehicleAvailability();
        availability.setAvailID(1L);
        availability.setVehicle(vehicle);
        availability.setDate(LocalDateTime.now());
        availability.setStartTime(LocalDateTime.now().plusHours(1));
        availability.setEndTime(LocalDateTime.now().plusHours(3));
        availability.setReasonNote("Maintenance");
        availability.setStatus("UNAVAILABLE");
    }

    // ── POST /cargoRoute/vehicleAvailability/createNewVehicleAvailability ─────

    @Test
    void createAvailability_ShouldReturn201_WhenCreated() throws Exception {
        when(service.save(any(VehicleAvailability.class)))
                .thenReturn(availability);

        mockMvc.perform(post("/cargoRoute/vehicleAvailability/createNewVehicleAvailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availability)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Vehicle availability created successfully."))
                .andExpect(jsonPath("$.vehicleAvailability.availID")
                        .value(1));
    }

    // ── GET /cargoRoute/vehicleAvailability/getAllVehicleAvailabilities ───────

    @Test
    void getAllAvailabilities_ShouldReturn200_WithList() throws Exception {
        when(service.getAll()).thenReturn(List.of(availability));

        mockMvc.perform(get("/cargoRoute/vehicleAvailability/getAllVehicleAvailabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("UNAVAILABLE"));
    }

    // ── GET /cargoRoute/vehicleAvailability/getVehicleAvailability/{id} ───────

    @Test
    void getAvailabilityById_ShouldReturn200_WhenFound() throws Exception {
        when(service.getById(1L)).thenReturn(availability);

        mockMvc.perform(get("/cargoRoute/vehicleAvailability/getVehicleAvailability/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availID").value(1));
    }

    @Test
    void getAvailabilityById_ShouldReturn404_WhenNotFound() throws Exception {
        when(service.getById(99L))
                .thenThrow(new ResourceNotFoundException("Availability not found"));

        mockMvc.perform(get("/cargoRoute/vehicleAvailability/getVehicleAvailability/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/vehicleAvailability/vehicle/{vehicleId} ──────────────

    @Test
    void getAvailabilitiesByVehicleId_ShouldReturn200() throws Exception {
        when(service.getByVehicleId(10L)).thenReturn(List.of(availability));

        mockMvc.perform(get("/cargoRoute/vehicleAvailability/vehicle/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── PUT /cargoRoute/vehicleAvailability/updateVehicleAvailability/{id} ───

    @Test
    void updateAvailability_ShouldReturn200_WhenUpdated() throws Exception {
        availability.setStatus("ACTIVE");

        when(service.update(eq(1L), any(VehicleAvailability.class)))
                .thenReturn(availability);

        mockMvc.perform(put("/cargoRoute/vehicleAvailability/updateVehicleAvailability/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availability)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ── DELETE /cargoRoute/vehicleAvailability/{id} ──────────────────────────

    @Test
    void deleteAvailability_ShouldReturn200_WhenDeleted() throws Exception {
        mockMvc.perform(delete("/cargoRoute/vehicleAvailability/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Vehicle availability deleted successfully for ID 1."));
    }
}
