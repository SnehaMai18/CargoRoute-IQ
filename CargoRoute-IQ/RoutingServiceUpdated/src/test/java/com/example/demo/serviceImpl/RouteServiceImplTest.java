package com.example.demo.serviceImpl;

import com.example.demo.dto.LoadDTO;
import com.example.demo.dto.RouteDTO;
import com.example.demo.entity.Load;
import com.example.demo.entity.Route;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoadRepository;
import com.example.demo.repository.RouteRepository;
import com.example.demo.serviceImpl.RouteServiceImpl;

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
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private LoadRepository loadRepository;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Route route;
    private Load load;
    private RouteDTO routeDTO;
    private LoadDTO loadDTO;

    @BeforeEach
    void setUp() {
        load = new Load();
        load.setLoadID(10L);
        load.setLoadCode("LOAD10");
        load.setVehicleID(100L);

        loadDTO = new LoadDTO();
        loadDTO.setLoadID(10L);

        route = new Route();
        route.setRouteID(1L);
        route.setLoad(load);
        route.setDistanceKm(150.0);
        route.setStatus("ACTIVE");

        routeDTO = new RouteDTO();
        routeDTO.setLoad(loadDTO);
        routeDTO.setDistanceKm(150.0);
        routeDTO.setStatus("ACTIVE");
    }

    // --- createRoute ---

    @Test
    void createRoute_ShouldReturnSavedRouteDTO() {
        when(loadRepository.findById(10L)).thenReturn(Optional.of(load));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        RouteDTO result = routeService.createRoute(routeDTO);

        assertNotNull(result);
        assertEquals(1L, result.getRouteID());
        assertEquals(10L, result.getLoad().getLoadID());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void createRoute_ShouldThrowException_WhenLoadNotFound() {
        when(loadRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.createRoute(routeDTO));
        verify(routeRepository, never()).save(any());
    }

    // --- getRouteById ---

    @Test
    void getRouteById_ShouldReturnRoute_WhenFound() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        RouteDTO result = routeService.getRouteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getRouteID());
    }

    @Test
    void getRouteById_ShouldThrowException_WhenNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> routeService.getRouteById(99L));
    }

    // --- updateRoute ---

    @Test
    void updateRoute_ShouldUpdateAndReturnDTO() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        routeDTO.setStatus("COMPLETED");
        RouteDTO result = routeService.updateRoute(1L, routeDTO);

        assertNotNull(result);
        verify(routeRepository).save(any(Route.class));
    }

    // --- deleteRoute ---

    @Test
    void deleteRoute_ShouldDelete_WhenRouteExists() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        routeService.deleteRoute(1L);

        verify(routeRepository).delete(route);
    }

    // --- Filter Methods ---

    @Test
    void getRoutesByLoadId_ShouldReturnFilteredList() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<RouteDTO> results = routeService.getRoutesByLoadId(10L);

        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getLoad().getLoadID());
    }

    @Test
    void getRoutesByVehicleId_ShouldReturnFilteredList() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<RouteDTO> results = routeService.getRoutesByVehicleId(100L);

        assertEquals(1, results.size());
        verify(routeRepository).findAll();
    }

    @Test
    void getRoutesByVehicleId_ShouldReturnEmpty_WhenNoMatch() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        List<RouteDTO> results = routeService.getRoutesByVehicleId(999L); // Wrong Vehicle ID

        assertTrue(results.isEmpty());
    }
}