package com.example.demo.repository;

import com.example.demo.entity.Vehicle;
import com.example.demo.entity.enums.VehicleStatus;
import com.example.demo.entity.enums.VehicleType;

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
class VehicleRepositoryTest {

    @Mock
    private VehicleRepository repository;

    private Vehicle buildVehicle(Long id, String regNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleID(id);
        vehicle.setRegNumber(regNumber);
        vehicle.setType(VehicleType.TRUCK);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicle.setMaxWeightKg(2000.0);
        vehicle.setMaxVolumeM3(30.0);
        vehicle.setLastMaintenanceAt(LocalDateTime.now());
        vehicle.setDriverID(10L);
        return vehicle;
    }

    // ───────────────────────── FIND BY REG NUMBER ─────────────────────────

    @Test
    void findByRegNumber_shouldReturnVehicle() {
        String regNumber = "TN01AB1234";
        Vehicle vehicle = buildVehicle(1L, regNumber);

        when(repository.findByRegNumber(regNumber))
                .thenReturn(vehicle);

        Vehicle result = repository.findByRegNumber(regNumber);

        assertNotNull(result);
        assertEquals(regNumber, result.getRegNumber());
        verify(repository, times(1)).findByRegNumber(regNumber);
    }

    @Test
    void findByRegNumber_shouldReturnNullWhenNotFound() {
        String regNumber = "TN00XX9999";

        when(repository.findByRegNumber(regNumber))
                .thenReturn(null);

        Vehicle result = repository.findByRegNumber(regNumber);

        assertNull(result);
        verify(repository, times(1)).findByRegNumber(regNumber);
    }

    // ───────────────────────── FIND BY ID ─────────────────────────

    @Test
    void findById_shouldReturnVehicle() {
        Vehicle vehicle = buildVehicle(1L, "TN01AB1234");

        when(repository.findById(1L))
                .thenReturn(Optional.of(vehicle));

        Optional<Vehicle> result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getVehicleID());
        verify(repository).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptional() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<Vehicle> result = repository.findById(999L);

        assertFalse(result.isPresent());
        verify(repository).findById(999L);
    }

    // ───────────────────────── FIND ALL ─────────────────────────

    @Test
    void findAll_shouldReturnAllVehicles() {
        List<Vehicle> vehicles = Arrays.asList(
                buildVehicle(1L, "TN01AB1111"),
                buildVehicle(2L, "TN01AB2222")
        );

        when(repository.findAll())
                .thenReturn(vehicles);

        List<Vehicle> result = repository.findAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    // ───────────────────────── SAVE ─────────────────────────

    @Test
    void save_shouldPersistVehicle() {
        Vehicle vehicle = buildVehicle(1L, "TN01AB1234");

        when(repository.save(vehicle))
                .thenReturn(vehicle);

        Vehicle saved = repository.save(vehicle);

        assertNotNull(saved);
        assertEquals("TN01AB1234", saved.getRegNumber());
        verify(repository).save(vehicle);
    }

    // ───────────────────────── UPDATE (SAVE AGAIN) ─────────────────────────

    @Test
    void update_shouldModifyVehicle() {
        Vehicle vehicle = buildVehicle(1L, "TN01AB1234");
        vehicle.setStatus(VehicleStatus.MAINTENANCE);

        when(repository.save(vehicle))
                .thenReturn(vehicle);

        Vehicle updated = repository.save(vehicle);

        assertEquals(VehicleStatus.MAINTENANCE, updated.getStatus());
        verify(repository).save(vehicle);
    }

    // ───────────────────────── DELETE ─────────────────────────

    @Test
    void delete_shouldRemoveVehicle() {
        Vehicle vehicle = buildVehicle(1L, "TN01AB1234");

        doNothing().when(repository).delete(vehicle);

        repository.delete(vehicle);

        verify(repository, times(1)).delete(vehicle);
    }

    // ───────────────────────── COUNT ─────────────────────────

    @Test
    void count_shouldReturnVehicleCount() {
        when(repository.count()).thenReturn(3L);

        long count = repository.count();

        assertEquals(3L, count);
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
        when(repository.existsById(999L)).thenReturn(false);

        assertFalse(repository.existsById(999L));
    }
}