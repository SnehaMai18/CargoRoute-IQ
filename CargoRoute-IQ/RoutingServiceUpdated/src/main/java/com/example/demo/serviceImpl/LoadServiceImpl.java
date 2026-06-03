package com.example.demo.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

import com.example.demo.clients.NotificationClient;
import com.example.demo.clients.TaskClient;
import com.example.demo.dto.LoadDTO;
import com.example.demo.dto.RequiredResponseDTO;
import com.example.demo.dto.VehicleDTO;
import com.example.demo.entity.Load;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.LoadRepository;
import com.example.demo.service.LoadService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class LoadServiceImpl implements LoadService {

    private static final String LOAD_SERVICE_CB = "loadServiceCB";

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private TaskClient taskClient;

    @Override
    public List<RequiredResponseDTO> getAllLoads() {
        return loadRepository.findAll()
                .stream()
                .map(load -> getLoadById(load.getLoadID())) 
                .collect(Collectors.toList());
    }

    @Override
    public LoadDTO createLoad(LoadDTO loadDTO) {
        Load load = dtoToEntity(loadDTO);
        Load saved = loadRepository.save(load);

        notificationClient.notifyUser(
                getCurrentUserId(),
                saved.getLoadID(),
                "Load " + saved.getLoadCode() + " planned and pending dispatch approval.",
                "Pickup"
        );
       
        taskClient.createTask(
            getCurrentUserId(),
            saved.getLoadID(),
            "Review and approve load " + saved.getLoadCode() + " for pickup planning.",
            saved.getPlannedStart() != null ? saved.getPlannedStart().toLocalDate() : null
        );

        return entityToDto(saved);
    }

    @Override
    public LoadDTO updateLoad(Long id, LoadDTO loadDTO) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));

        load.setLoadCode(loadDTO.getLoadCode());
        load.setVehicleID(loadDTO.getVehicleID());
        load.setPlannedStart(loadDTO.getPlannedStart());
        load.setPlannedEnd(loadDTO.getPlannedEnd());
        load.setTotalWeightKg(loadDTO.getTotalWeightKg());
        load.setTotalVolumeM3(loadDTO.getTotalVolumeM3());
        load.setBookingsJSON(loadDTO.getBookingsJSON());
        load.setStatus(loadDTO.getStatus());

        Load updated = loadRepository.save(load);

        String category = "Delivery";
        String message = "Load " + updated.getLoadCode() + " updated with status " + updated.getStatus() + ".";
        notificationClient.notifyUser(getCurrentUserId(), updated.getLoadID(), message, category);
        taskClient.createTask(
            getCurrentUserId(),
            updated.getLoadID(),
            "Re-validate delivery execution for load " + updated.getLoadCode() + ".",
            updated.getPlannedEnd() != null ? updated.getPlannedEnd().toLocalDate() : null
        );

        return entityToDto(updated);
    }

    @Override
    public void deleteLoad(Long id) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));
        loadRepository.delete(load);
    }

    @Override
    @CircuitBreaker(name = LOAD_SERVICE_CB, fallbackMethod = "getRequiredResponseFallback")
    public RequiredResponseDTO getLoadById(Long id) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));

        RequiredResponseDTO response = new RequiredResponseDTO();
        response.setLoad(entityToDto(load));

        if (load.getVehicleID() != null) {
           
            VehicleDTO vehicle = restTemplate.getForObject(
                    "http://FLEET-SERVICE/cargoRoute/vehicles/getVehicle/" + load.getVehicleID(),
                    VehicleDTO.class
            );
            response.setVehicle(vehicle);
        }

        return response;
    }

    // Fallback method
    public RequiredResponseDTO getRequiredResponseFallback(Long id, Throwable t) {
        RequiredResponseDTO fallback = new RequiredResponseDTO();
        LoadDTO loadDto = new LoadDTO();
        loadDto.setLoadID(id);
        loadDto.setStatus("UNAVAILABLE");
        loadDto.setLoadCode("FALLBACK");
        fallback.setLoad(loadDto);
        return fallback;
    }

    // Mapping methods
    private LoadDTO entityToDto(Load load) {
        LoadDTO dto = new LoadDTO();
        dto.setLoadID(load.getLoadID());
        dto.setLoadCode(load.getLoadCode());
        dto.setVehicleID(load.getVehicleID());
        dto.setPlannedStart(load.getPlannedStart());
        dto.setPlannedEnd(load.getPlannedEnd());
        dto.setTotalWeightKg(load.getTotalWeightKg());
        dto.setTotalVolumeM3(load.getTotalVolumeM3());
        dto.setBookingsJSON(load.getBookingsJSON());
        dto.setStatus(load.getStatus());
        return dto;
    }

    private Load dtoToEntity(LoadDTO dto) {
        Load load = new Load();
        load.setLoadID(dto.getLoadID());
        load.setLoadCode(dto.getLoadCode());
        load.setVehicleID(dto.getVehicleID());
        load.setPlannedStart(dto.getPlannedStart());
        load.setPlannedEnd(dto.getPlannedEnd());
        load.setTotalWeightKg(dto.getTotalWeightKg());
        load.setTotalVolumeM3(dto.getTotalVolumeM3());
        load.setBookingsJSON(dto.getBookingsJSON());
        load.setStatus(dto.getStatus());
        return load;
    }

    private Long getCurrentUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            Object userIdObj = servletAttributes.getRequest().getAttribute("userId");
            if (userIdObj instanceof Long userId) {
                return userId;
            }
            if (userIdObj instanceof Integer userId) {
                return userId.longValue();
            }
            if (userIdObj instanceof String userId) {
                try {
                    return Long.parseLong(userId);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}