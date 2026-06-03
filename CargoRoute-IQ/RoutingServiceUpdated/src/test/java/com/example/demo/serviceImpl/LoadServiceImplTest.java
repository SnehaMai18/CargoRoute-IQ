package com.example.demo.serviceImpl;

import com.example.demo.dto.LoadDTO;
import com.example.demo.dto.RequiredResponseDTO;
import com.example.demo.dto.VehicleDTO;
import com.example.demo.entity.Load;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoadRepository;
import com.example.demo.serviceImpl.LoadServiceImpl;
import com.example.demo.clients.NotificationClient;
import com.example.demo.clients.TaskClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadServiceImplTest {

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private RestTemplate restTemplate;

    // These must stay mocked so the code doesn't throw NullPointerExceptions,
    // but we won't write "verify" or "when" statements for them.
    @Mock
    private NotificationClient notificationClient;

    @Mock
    private TaskClient taskClient;

    @InjectMocks
    private LoadServiceImpl loadService;

    private Load load;
    private LoadDTO loadDTO;
    private MockedStatic<RequestContextHolder> mockedRequestContext;

    @BeforeEach
    void setUp() {
        load = new Load();
        load.setLoadID(1L);
        load.setLoadCode("LOAD-001");
        load.setVehicleID(10L);
        load.setStatus("PLANNED");

        loadDTO = new LoadDTO();
        loadDTO.setLoadCode("LOAD-001");
        loadDTO.setVehicleID(10L);
        loadDTO.setStatus("PLANNED");

        // Necessary to prevent NullPointer in getCurrentUserId()
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        mockedRequestContext = mockStatic(RequestContextHolder.class);
        mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
    }

    @AfterEach
    void tearDown() {
        mockedRequestContext.close();
    }

    // --- createLoad ---

    @Test
    void createLoad_ShouldReturnSavedDTO() {
        // Arrange
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        // Act
        LoadDTO result = loadService.createLoad(loadDTO);

        // Assert
        assertNotNull(result);
        assertEquals("LOAD-001", result.getLoadCode());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    // --- updateLoad ---

    @Test
    void updateLoad_ShouldUpdateFieldsAndSave() {
        // Arrange
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(loadRepository.save(any(Load.class))).thenReturn(load);

        // Act
        LoadDTO result = loadService.updateLoad(1L, loadDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getLoadID());
        verify(loadRepository).save(any(Load.class));
    }

    // --- getLoadById ---

    @Test
    void getLoadById_ShouldReturnEnrichedResponse() {
        // Arrange
        VehicleDTO vehicleDTO = new VehicleDTO();
        vehicleDTO.setVehicleID(10L);

        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(restTemplate.getForObject(contains("vehicles/getVehicle/"), eq(VehicleDTO.class)))
                .thenReturn(vehicleDTO);

        // Act
        RequiredResponseDTO response = loadService.getLoadById(1L);

        // Assert
        assertNotNull(response.getLoad());
        assertNotNull(response.getVehicle());
        assertEquals(10L, response.getVehicle().getVehicleID());
    }

    @Test
    void getLoadById_ShouldThrowException_WhenIdNotFound() {
        // Arrange
        when(loadRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> loadService.getLoadById(99L));
    }

    // --- deleteLoad ---

    @Test
    void deleteLoad_ShouldInvokeRepositoryDelete() {
        // Arrange
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));

        // Act
        loadService.deleteLoad(1L);

        // Assert
        verify(loadRepository, times(1)).delete(load);
    }

    // --- Fallback ---

    @Test
    void getRequiredResponseFallback_ShouldReturnFallbackDTO() {
        // Act
        RequiredResponseDTO result = loadService.getRequiredResponseFallback(1L, new RuntimeException());

        // Assert
        assertEquals("FALLBACK", result.getLoad().getLoadCode());
        assertEquals("UNAVAILABLE", result.getLoad().getStatus());
    }
}