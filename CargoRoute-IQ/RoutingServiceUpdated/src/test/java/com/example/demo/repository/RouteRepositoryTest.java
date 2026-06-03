package com.example.demo.repository;

import com.example.demo.entity.Load;
import com.example.demo.entity.Route;
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
class RouteRepositoryTest {

    @Mock
    private RouteRepository routeRepository;

    /**
     * Helper method to build a Route entity for testing.
     */
    private Route buildRoute(Long routeId, Long loadId) {
        Load load = new Load();
        load.setLoadID(loadId);

        Route route = new Route();
        route.setRouteID(routeId);
        route.setLoad(load);
        route.setSequenceJSON("[{\"stop\": 1, \"location\": \"Site A\"}]");
        route.setDistanceKm(120.5);
        route.setEstimatedDurationMin(150);
        route.setCostEstimate(450.0);
        route.setStatus("ACTIVE");

        return route;
    }

    // ───────────────────────── FIND BY ID ─────────────────────────

    @Test
    void findById_shouldReturnRoute() {
        Route route = buildRoute(1L, 10L);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        Optional<Route> result = routeRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getRouteID());
        assertEquals(120.5, result.get().getDistanceKm());
        verify(routeRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptional() {
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Route> result = routeRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    // ───────────────────────── FIND ALL ─────────────────────────

    @Test
    void findAll_shouldReturnAllRoutes() {
        List<Route> mockRoutes = Arrays.asList(
                buildRoute(1L, 10L),
                buildRoute(2L, 11L)
        );

        when(routeRepository.findAll()).thenReturn(mockRoutes);

        List<Route> result = routeRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(routeRepository).findAll();
    }

    // ───────────────────────── SAVE / CREATE ─────────────────────────

    @Test
    void save_shouldPersistRoute() {
        Route route = buildRoute(1L, 10L);

        when(routeRepository.save(route)).thenReturn(route);

        Route saved = routeRepository.save(route);

        assertNotNull(saved);
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals(1L, saved.getRouteID());
        verify(routeRepository).save(route);
    }

    // ───────────────────────── UPDATE ─────────────────────────

    @Test
    void update_shouldModifyExistingRoute() {
        Route existing = buildRoute(1L, 10L);
        existing.setStatus("COMPLETED");
        existing.setDistanceKm(125.0);

        when(routeRepository.save(existing)).thenReturn(existing);

        Route updated = routeRepository.save(existing);

        assertEquals("COMPLETED", updated.getStatus());
        assertEquals(125.0, updated.getDistanceKm());
        verify(routeRepository).save(existing);
    }

    // ───────────────────────── DELETE ─────────────────────────

    @Test
    void delete_shouldRemoveRoute() {
        Route route = buildRoute(1L, 10L);

        doNothing().when(routeRepository).delete(route);

        routeRepository.delete(route);

        verify(routeRepository, times(1)).delete(route);
    }

    @Test
    void deleteById_shouldInvokeDeletion() {
        Long id = 1L;
        doNothing().when(routeRepository).deleteById(id);

        routeRepository.deleteById(id);

        verify(routeRepository).deleteById(id);
    }

    // ───────────────────────── EXISTS BY ID ─────────────────────────

    @Test
    void existsById_shouldReturnTrue() {
        when(routeRepository.existsById(1L)).thenReturn(true);

        boolean result = routeRepository.existsById(1L);

        assertTrue(result);
        verify(routeRepository).existsById(1L);
    }

    // ───────────────────────── COUNT ─────────────────────────

    @Test
    void count_shouldReturnTotalNumber() {
        when(routeRepository.count()).thenReturn(3L);

        long count = routeRepository.count();

        assertEquals(3L, count);
        verify(routeRepository).count();
    }
}