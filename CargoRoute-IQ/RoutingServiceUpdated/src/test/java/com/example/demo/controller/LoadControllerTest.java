package com.example.demo.controller;

import com.example.demo.dto.LoadDTO;
import com.example.demo.dto.RequiredResponseDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.LoadService;
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
class LoadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoadService loadService;

    @InjectMocks
    private LoadController loadController;

    private ObjectMapper objectMapper;
    private LoadDTO loadDTO;
    private RequiredResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Build MockMvc with the Global Exception Handler to catch ResourceNotFoundException
        mockMvc = MockMvcBuilders.standaloneSetup(loadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        loadDTO = new LoadDTO();
        loadDTO.setLoadID(1L);
        loadDTO.setLoadCode("LOAD-001");
        loadDTO.setStatus("PENDING");

        responseDTO = new RequiredResponseDTO();
        responseDTO.setLoad(loadDTO);
    }

    // ── POST /cargoRoute/loads/createNewLoad ──────────────────────────────────

    @Test
    void createLoad_ShouldReturn201_WhenCreatedSuccessfully() throws Exception {
        when(loadService.createLoad(any(LoadDTO.class))).thenReturn(loadDTO);

        mockMvc.perform(post("/cargoRoute/loads/createNewLoad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Load has been created successfully"));
    }

    // ── PUT /cargoRoute/loads/updateLoad/{id} ──────────────────────────────────

    @Test
    void updateLoad_ShouldReturn200_WhenUpdatedSuccessfully() throws Exception {
        when(loadService.updateLoad(eq(1L), any(LoadDTO.class))).thenReturn(loadDTO);

        mockMvc.perform(put("/cargoRoute/loads/updateLoad/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Load updated successfully for ID 1."))
                .andExpect(jsonPath("$.load.loadCode").value("LOAD-001"));
    }

    @Test
    void updateLoad_ShouldReturn404_WhenLoadNotFound() throws Exception {
        when(loadService.updateLoad(eq(99L), any(LoadDTO.class)))
                .thenThrow(new ResourceNotFoundException("Load not found with id: 99"));

        mockMvc.perform(put("/cargoRoute/loads/updateLoad/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDTO)))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/loads/getLoad/{id} ───────────────────────────────────

    @Test
    void getLoadById_ShouldReturn200_WhenFound() throws Exception {
        when(loadService.getLoadById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/cargoRoute/loads/getLoad/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.load.loadCode").value("LOAD-001"));
    }

    @Test
    void getLoadById_ShouldReturn404_WhenNotFound() throws Exception {
        when(loadService.getLoadById(99L))
                .thenThrow(new ResourceNotFoundException("Load not found with id: 99"));

        mockMvc.perform(get("/cargoRoute/loads/getLoad/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/loads/getAllLoads ────────────────────────────────────

    @Test
    void getAllLoads_ShouldReturn200_WithList() throws Exception {
        when(loadService.getAllLoads()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/cargoRoute/loads/getAllLoads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].load.loadCode").value("LOAD-001"));
    }

    // ── DELETE /cargoRoute/loads/deleteLoad/{id} ─────────────────────────────

    @Test
    void deleteLoad_ShouldReturn200_WhenDeleted() throws Exception {
        // deleteLoad is a void method in service, so no "when" is strictly needed for success
        
        mockMvc.perform(delete("/cargoRoute/loads/deleteLoad/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Load deleted successfully for ID 1."));
    }

    @Test
    void deleteLoad_ShouldReturn404_WhenIdDoesNotExist() throws Exception {
        // Mocking a void method to throw an exception
        doThrow(new ResourceNotFoundException("Load not found")).when(loadService).deleteLoad(99L);

        mockMvc.perform(delete("/cargoRoute/loads/deleteLoad/99"))
                .andExpect(status().isNotFound());
    }
}