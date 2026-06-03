package com.example.demo.repository;

import com.example.demo.entity.Vehicle;
import com.example.demo.entity.VehicleAvailability;

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
class VehicleAvailabilityRepositoryTest {

    @Mock
    private VehicleAvailabilityRepository repository;

    private VehicleAvailability buildAvailability(Long availId, Long vehicleId) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleID(vehicleId);

        VehicleAvailability availability = new VehicleAvailability();
        availability.setAvailID(availId);
        availability.setVehicle(vehicle);
        availability.setDate(LocalDateTime.now());
        availability.setStartTime(LocalDateTime.now().plusHours(1));
        availability.setEndTime(LocalDateTime.now().plusHours(2));
        availability.setReasonNote("Maintenance");
        availability.setStatus("UNAVAILABLE");

        return availability;
    }

    // ───────────────────────── FIND BY VEHICLE ID ─────────────────────────

    @Test
    void findByVehicle_VehicleID_shouldReturnAvailabilities() {
        Long vehicleId = 1L;

        List<VehicleAvailability> mockData = Arrays.asList(
                buildAvailability(1L, vehicleId),
                buildAvailability(2L, vehicleId)
        );

        when(repository.findByVehicle_VehicleID(vehicleId)).thenReturn(mockData);

        List<VehicleAvailability> result =
                repository.findByVehicle_VehicleID(vehicleId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findByVehicle_VehicleID(vehicleId);
    }

    @Test
    void findByVehicle_VehicleID_shouldReturnEmptyList() {
        Long vehicleId = 99L;

        when(repository.findByVehicle_VehicleID(vehicleId))
                .thenReturn(List.of());

        List<VehicleAvailability> result =
                repository.findByVehicle_VehicleID(vehicleId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ───────────────────────── FIND BY ID ─────────────────────────

    @Test
    void findById_shouldReturnAvailability() {
        VehicleAvailability availability =
                buildAvailability(1L, 10L);

        when(repository.findById(1L))
                .thenReturn(Optional.of(availability));

        Optional<VehicleAvailability> result =
                repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getAvailID());
    }

    @Test
    void findById_shouldReturnEmptyOptional() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<VehicleAvailability> result =
                repository.findById(999L);

        assertFalse(result.isPresent());
    }

    // ───────────────────────── SAVE ─────────────────────────

    @Test
    void save_shouldPersistAvailability() {
        VehicleAvailability availability =
                buildAvailability(1L, 10L);

        when(repository.save(availability))
                .thenReturn(availability);

        VehicleAvailability saved =
                repository.save(availability);

        assertNotNull(saved);
        assertEquals("UNAVAILABLE", saved.getStatus());
        verify(repository).save(availability);
    }

    // ───────────────────────── UPDATE ─────────────────────────

    @Test
    void update_shouldModifyExistingAvailability() {
        VehicleAvailability existing =
                buildAvailability(1L, 10L);

        existing.setStatus("ACTIVE");

        when(repository.save(existing))
                .thenReturn(existing);

        VehicleAvailability updated =
                repository.save(existing);

        assertEquals("ACTIVE", updated.getStatus());
        verify(repository).save(existing);
    }

    // ───────────────────────── DELETE ─────────────────────────

    @Test
    void delete_shouldRemoveAvailability() {
        VehicleAvailability availability =
                buildAvailability(1L, 10L);

        doNothing().when(repository).delete(availability);

        repository.delete(availability);

        verify(repository, times(1)).delete(availability);
    }

    // ───────────────────────── COUNT ─────────────────────────

    @Test
    void count_shouldReturnNumberOfAvailabilities() {
        when(repository.count()).thenReturn(5L);

        long count = repository.count();

        assertEquals(5L, count);
        verify(repository).count();
    }

    // ───────────────────────── EXISTS BY ID ─────────────────────────

    @Test
    void existsById_shouldReturnTrue() {
        when(repository.existsById(1L)).thenReturn(true);

        assertTrue(repository.existsById(1L));
    }

    @Test
    void existsById_shouldReturnFalse() {
        when(repository.existsById(99L)).thenReturn(false);

        assertFalse(repository.existsById(99L));
    }
}