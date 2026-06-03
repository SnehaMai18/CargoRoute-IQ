package com.example.demo.serviceImpl;

import com.example.demo.dto.RoutingRuleDTO;
import com.example.demo.entity.RoutingRule;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RoutingRuleRepository;
import com.example.demo.serviceImpl.RoutingRuleServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingRuleServiceImplTest {

    @Mock
    private RoutingRuleRepository ruleRepository;

    @InjectMocks
    private RoutingRuleServiceImpl routingRuleService;

    private RoutingRule rule;
    private RoutingRuleDTO ruleDTO;

    @BeforeEach
    void setUp() {
        rule = new RoutingRule();
        rule.setRuleID(1L);
        rule.setName("Express Route");
        rule.setConditionsJSON("{\"weight\": \">500\"}");
        rule.setPriority(1);
        rule.setActive(true);

        ruleDTO = new RoutingRuleDTO();
        ruleDTO.setRuleID(1L);
        ruleDTO.setName("Express Route");
        ruleDTO.setConditionsJSON("{\"weight\": \">500\"}");
        ruleDTO.setPriority(1);
        ruleDTO.setActive(true);
    }

    // --- createRule ---

    @Test
    void createRule_ShouldSaveAndReturnDTO() {
        when(ruleRepository.save(any(RoutingRule.class))).thenReturn(rule);

        RoutingRuleDTO result = routingRuleService.createRule(ruleDTO);

        assertNotNull(result);
        assertEquals("Express Route", result.getName());
        verify(ruleRepository, times(1)).save(any(RoutingRule.class));
    }

    // --- getRuleById ---

    @Test
    void getRuleById_ShouldReturnDTO_WhenFound() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        RoutingRuleDTO result = routingRuleService.getRuleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getRuleID());
    }

    @Test
    void getRuleById_ShouldThrowException_WhenNotFound() {
        when(ruleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routingRuleService.getRuleById(99L));
    }

    // --- updateRule ---

    @Test
    void updateRule_ShouldUpdateExistingRule() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(RoutingRule.class))).thenReturn(rule);

        ruleDTO.setName("Updated Name");
        RoutingRuleDTO result = routingRuleService.updateRule(1L, ruleDTO);

        assertNotNull(result);
        verify(ruleRepository).save(any(RoutingRule.class));
    }

    // --- deleteRule ---

    @Test
    void deleteRule_ShouldInvokeDelete_WhenExists() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        routingRuleService.deleteRule(1L);

        verify(ruleRepository, times(1)).delete(rule);
    }

    // --- getActiveRules ---

    @Test
    void getActiveRules_ShouldReturnOnlyActiveRules() {
        when(ruleRepository.findByActiveTrue()).thenReturn(List.of(rule));

        List<RoutingRuleDTO> results = routingRuleService.getActiveRules();

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertTrue(results.get(0).getActive());
    }

    // --- getAllRules ---

    @Test
    void getAllRules_ShouldReturnList() {
        when(ruleRepository.findAll()).thenReturn(List.of(rule));

        List<RoutingRuleDTO> results = routingRuleService.getAllRules();

        assertEquals(1, results.size());
        verify(ruleRepository).findAll();
    }
}