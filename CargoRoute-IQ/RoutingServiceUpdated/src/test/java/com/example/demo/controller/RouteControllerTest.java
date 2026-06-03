package com.example.demo.controller;

import com.example.demo.dto.LoadDTO;
import com.example.demo.dto.RouteDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.RouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteController routeController;

    private ObjectMapper objectMapper;
    private RouteDTO routeDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(routeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        LoadDTO loadDTO = new LoadDTO();
        loadDTO.setLoadID(10L);

        routeDTO = new RouteDTO();
        routeDTO.setRouteID(1L);
        routeDTO.setLoad(loadDTO);
        routeDTO.setDistanceKm(150.0);
        routeDTO.setStatus("ACTIVE");
    }

    // ── GET /cargoRoute/routes/getRoute/{id} ─────────────────────────────────

    @Test
    void getRouteById_ShouldReturn200_WhenFound() throws Exception {
        when(routeService.getRouteById(1L)).thenReturn(routeDTO);

        mockMvc.perform(get("/cargoRoute/routes/getRoute/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeID").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getRouteById_ShouldReturn404_WhenNotFound() throws Exception {
        when(routeService.getRouteById(99L))
                .thenThrow(new ResourceNotFoundException("Route not found with id: 99"));

        mockMvc.perform(get("/cargoRoute/routes/getRoute/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /cargoRoute/routes/createNewRoute ───────────────────────────────

    @Test
    void createRoute_ShouldReturn200_WhenCreated() throws Exception {
        when(routeService.createRoute(any(RouteDTO.class))).thenReturn(routeDTO);

        mockMvc.perform(post("/cargoRoute/routes/createNewRoute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Route created successfully"))
                .andExpect(jsonPath("$.data.routeID").value(1));
    }

    // ── PUT /cargoRoute/routes/updateRoute/{id} ───────────────────────────────

    @Test
    void updateRoute_ShouldReturn200_WhenUpdated() throws Exception {
        when(routeService.updateRoute(eq(1L), any(RouteDTO.class))).thenReturn(routeDTO);

        mockMvc.perform(put("/cargoRoute/routes/updateRoute/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Route updated successfully with id: 1"));
    }

    // ── DELETE /cargoRoute/routes/deleteRoute/{id} ───────────────────────────

    @Test
    void deleteRoute_ShouldReturn200_WhenDeleted() throws Exception {
        mockMvc.perform(delete("/cargoRoute/routes/deleteRoute/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Route deleted with id: 1"));
    }

    @Test
    void deleteRoute_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(routeService).deleteRoute(99L);

        mockMvc.perform(delete("/cargoRoute/routes/deleteRoute/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/routes/vehicle/{vehicleId} ───────────────────────────

    @Test
    void getRoutesByVehicleId_ShouldReturnList() throws Exception {
        when(routeService.getRoutesByVehicleId(100L)).thenReturn(List.of(routeDTO));

        mockMvc.perform(get("/cargoRoute/routes/vehicle/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /cargoRoute/routes/load/{loadId} ─────────────────────────────────

    @Test
    void getRoutesByLoadId_ShouldReturnList() throws Exception {
        when(routeService.getRoutesByLoadId(10L)).thenReturn(List.of(routeDTO));

        mockMvc.perform(get("/cargoRoute/routes/load/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].load.loadID").value(10));
    }
}