package com.example.demo.controller;

import com.example.demo.dto.RoutingRuleDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.RoutingRuleService;
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
class RoutingRuleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoutingRuleService ruleService;

    @InjectMocks
    private RoutingRuleController routingRuleController;

    private ObjectMapper objectMapper;
    private RoutingRuleDTO ruleDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(routingRuleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        ruleDTO = new RoutingRuleDTO();
        ruleDTO.setRuleID(1L);
        ruleDTO.setName("Express Routing Rule");
        ruleDTO.setConditionsJSON("{\"minWeight\": 500}");
        ruleDTO.setPriority(1);
        ruleDTO.setActive(true);
    }

    // ── GET /cargoRoute/routingRules/getRoutingRule/{id} ───────────────────

    @Test
    void getRuleById_ShouldReturn200_WhenFound() throws Exception {
        when(ruleService.getRuleById(1L)).thenReturn(ruleDTO);

        mockMvc.perform(get("/cargoRoute/routingRules/getRoutingRule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleID").value(1))
                .andExpect(jsonPath("$.name").value("Express Routing Rule"));
    }

    @Test
    void getRuleById_ShouldReturn404_WhenNotFound() throws Exception {
        when(ruleService.getRuleById(99L))
                .thenThrow(new ResourceNotFoundException("RoutingRule not found with id: 99"));

        mockMvc.perform(get("/cargoRoute/routingRules/getRoutingRule/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/routingRules/getAllRoutingRules ───────────────────

    @Test
    void getAllRules_ShouldReturn200_WithList() throws Exception {
        when(ruleService.getAllRules()).thenReturn(List.of(ruleDTO));

        mockMvc.perform(get("/cargoRoute/routingRules/getAllRoutingRules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ruleID").value(1));
    }

    // ── POST /cargoRoute/routingRules/createNewRoutingRule ────────────────

    @Test
    void createRule_ShouldReturn200_WhenCreated() throws Exception {
        when(ruleService.createRule(any(RoutingRuleDTO.class))).thenReturn(ruleDTO);

        mockMvc.perform(post("/cargoRoute/routingRules/createNewRoutingRule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Routing rule created successfully"))
                .andExpect(jsonPath("$.data.ruleID").value(1));
    }

    // ── PUT /cargoRoute/routingRules/updateRoutingRule/{id} ────────────────

    @Test
    void updateRule_ShouldReturn200_WhenUpdated() throws Exception {
        when(ruleService.updateRule(eq(1L), any(RoutingRuleDTO.class))).thenReturn(ruleDTO);

        mockMvc.perform(put("/cargoRoute/routingRules/updateRoutingRule/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Routing rule updated successfully with id: 1"))
                .andExpect(jsonPath("$.data.name").value("Express Routing Rule"));
    }

    // ── DELETE /cargoRoute/routingRules/deleteRoutingRule/{id} ─────────────

    @Test
    void deleteRule_ShouldReturn200_WhenDeleted() throws Exception {
        mockMvc.perform(delete("/cargoRoute/routingRules/deleteRoutingRule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Routing rule deleted with id: 1"));
    }

    @Test
    void deleteRule_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(ruleService).deleteRule(99L);

        mockMvc.perform(delete("/cargoRoute/routingRules/deleteRoutingRule/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /cargoRoute/routingRules/active ────────────────────────────────

    @Test
    void getActiveRules_ShouldReturn200_WithActiveList() throws Exception {
        when(ruleService.getActiveRules()).thenReturn(List.of(ruleDTO));

        mockMvc.perform(get("/cargoRoute/routingRules/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }
}