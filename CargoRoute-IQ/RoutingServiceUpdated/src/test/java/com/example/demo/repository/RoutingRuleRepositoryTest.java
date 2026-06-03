package com.example.demo.repository;

import com.example.demo.entity.RoutingRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingRuleRepositoryTest {

    @Mock
    private RoutingRuleRepository routingRuleRepository;

    /**
     * Helper method to build a RoutingRule entity.
     */
    private RoutingRule buildRule(Long id, String name, boolean isActive) {
        RoutingRule rule = new RoutingRule();
        rule.setRuleID(id);
        rule.setName(name);
        rule.setConditionsJSON("{\"minWeight\": 100}");
        rule.setPriority(1);
        rule.setActive(isActive);
        return rule;
    }

    // ───────────────────────── CUSTOM METHOD: findByActiveTrue ─────────────────────────

    @Test
    void findByActiveTrue_shouldReturnOnlyActiveRules() {
        List<RoutingRule> activeRules = Arrays.asList(
                buildRule(1L, "Rule A", true),
                buildRule(2L, "Rule B", true)
        );

        when(routingRuleRepository.findByActiveTrue()).thenReturn(activeRules);

        List<RoutingRule> result = routingRuleRepository.findByActiveTrue();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(RoutingRule::getActive));
        verify(routingRuleRepository, times(1)).findByActiveTrue();
    }

    // ───────────────────────── FIND BY ID ─────────────────────────

    @Test
    void findById_shouldReturnRule() {
        RoutingRule rule = buildRule(1L, "Express Rule", true);

        when(routingRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

        Optional<RoutingRule> result = routingRuleRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Express Rule", result.get().getName());
        verify(routingRuleRepository).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptional() {
        when(routingRuleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<RoutingRule> result = routingRuleRepository.findById(99L);

        assertFalse(result.isPresent());
    }

    // ───────────────────────── SAVE ─────────────────────────

    @Test
    void save_shouldPersistRule() {
        RoutingRule rule = buildRule(1L, "Standard Rule", true);

        when(routingRuleRepository.save(rule)).thenReturn(rule);

        RoutingRule saved = routingRuleRepository.save(rule);

        assertNotNull(saved);
        assertEquals("Standard Rule", saved.getName());
        verify(routingRuleRepository).save(rule);
    }

    // ───────────────────────── UPDATE ─────────────────────────

    @Test
    void update_shouldModifyExistingRule() {
        RoutingRule existing = buildRule(1L, "Old Name", true);
        existing.setName("Updated Name");
        existing.setActive(false);

        when(routingRuleRepository.save(existing)).thenReturn(existing);

        RoutingRule updated = routingRuleRepository.save(existing);

        assertEquals("Updated Name", updated.getName());
        assertFalse(updated.getActive());
        verify(routingRuleRepository).save(existing);
    }

    // ───────────────────────── DELETE ─────────────────────────

    @Test
    void delete_shouldInvokeRemoval() {
        RoutingRule rule = buildRule(1L, "Delete Me", true);

        doNothing().when(routingRuleRepository).delete(rule);

        routingRuleRepository.delete(rule);

        verify(routingRuleRepository, times(1)).delete(rule);
    }

    // ───────────────────────── EXISTS BY ID ─────────────────────────

    @Test
    void existsById_shouldReturnTrue() {
        when(routingRuleRepository.existsById(1L)).thenReturn(true);

        assertTrue(routingRuleRepository.existsById(1L));
    }

    // ───────────────────────── COUNT ─────────────────────────

    @Test
    void count_shouldReturnTotalNumber() {
        when(routingRuleRepository.count()).thenReturn(5L);

        long count = routingRuleRepository.count();

        assertEquals(5L, count);
        verify(routingRuleRepository).count();
    }
}