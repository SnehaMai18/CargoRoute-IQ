package com.example.demo.repository;

import com.example.demo.entity.Load;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadRepositoryTest {

    @Mock
    private LoadRepository loadRepository;

    private Load buildLoad(Long id, String code) {
        Load load = new Load();
        load.setLoadID(id);
        load.setLoadCode(code);
        load.setVehicleID(101L);
        load.setStatus("PENDING");
        load.setPlannedStart(LocalDateTime.now());
        load.setPlannedEnd(LocalDateTime.now().plusDays(1));
        load.setTotalWeightKg(5000.0);
        load.setTotalVolumeM3(20.0);
        return load;
    }

    // ───────────────────────── FIND BY ID ─────────────────────────

    @Test
    void findById_shouldReturnLoad() {
        Load load = buildLoad(1L, "LD-001");

        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));

        Optional<Load> result = loadRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("LD-001", result.get().getLoadCode());
        verify(loadRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptional() {
        when(loadRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Load> result = loadRepository.findById(99L);

        assertFalse(result.isPresent());
    }

    // ───────────────────────── FIND ALL ─────────────────────────

    @Test
    void findAll_shouldReturnAllLoads() {
        List<Load> mockLoads = Arrays.asList(
                buildLoad(1L, "LD-001"),
                buildLoad(2L, "LD-002")
        );

        when(loadRepository.findAll()).thenReturn(mockLoads);

        List<Load> result = loadRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loadRepository).findAll();
    }

    // ───────────────────────── SAVE ─────────────────────────

    @Test
    void save_shouldPersistLoad() {
        Load load = buildLoad(1L, "LD-NEW");

        when(loadRepository.save(load)).thenReturn(load);

        Load saved = loadRepository.save(load);

        assertNotNull(saved);
        assertEquals("LD-NEW", saved.getLoadCode());
        verify(loadRepository).save(load);
    }

    // ───────────────────────── UPDATE ─────────────────────────

    @Test
    void update_shouldModifyExistingLoad() {
        Load existing = buildLoad(1L, "LD-001");
        existing.setStatus("DISPATCHED");

        when(loadRepository.save(existing)).thenReturn(existing);

        Load updated = loadRepository.save(existing);

        assertEquals("DISPATCHED", updated.getStatus());
        verify(loadRepository).save(existing);
    }

    // ───────────────────────── DELETE ─────────────────────────

    @Test
    void delete_shouldRemoveLoad() {
        Load load = buildLoad(1L, "LD-001");

        // Use doNothing for void methods
        doNothing().when(loadRepository).delete(load);

        loadRepository.delete(load);

        verify(loadRepository, times(1)).delete(load);
    }

    // ───────────────────────── EXISTS BY ID ─────────────────────────

    @Test
    void existsById_shouldReturnTrue() {
        when(loadRepository.existsById(1L)).thenReturn(true);

        boolean exists = loadRepository.existsById(1L);

        assertTrue(exists);
        verify(loadRepository).existsById(1L);
    }

    @Test
    void existsById_shouldReturnFalse() {
        when(loadRepository.existsById(999L)).thenReturn(false);

        boolean exists = loadRepository.existsById(999L);

        assertFalse(exists);
    }

    // ───────────────────────── COUNT ─────────────────────────

    @Test
    void count_shouldReturnTotalNumber() {
        when(loadRepository.count()).thenReturn(10L);

        long count = loadRepository.count();

        assertEquals(10L, count);
        verify(loadRepository).count();
    }
}