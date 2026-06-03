package com.example.demo.ServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.clients.NotificationClient;
import com.example.demo.clients.TaskClient;
import com.example.demo.service.InvoiceService;
import com.example.demo.dto.InvoiceDTO;
import com.example.demo.dto.InvoiceRequiredResponseDTO;
import com.example.demo.dto.ShipperDTO;
import com.example.demo.entity.Invoice;
import com.example.demo.exception.InvoiceNotFoundException;
import com.example.demo.repository.InvoiceRepository;

@Service
public class InvoiceServiceimpl implements InvoiceService {

    @Autowired
    private InvoiceRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private TaskClient taskClient;

    // ================= DTO → ENTITY =================
    private Invoice toEntity(InvoiceDTO dto) {
        Invoice entity = new Invoice();
        entity.setInvoiceID(dto.getInvoiceID());
        entity.setShipperID(dto.getShipperID());
        entity.setPeriodStart(dto.getPeriodStart().toLocalDate());
        entity.setPeriodEnd(dto.getPeriodEnd().toLocalDate());
        entity.setLinesJSON(dto.getLinesJSON());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setIssuedAt(LocalDateTime.now());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    // ================= ENTITY → DTO =================
    private InvoiceDTO toDTO(Invoice entity) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceID(entity.getInvoiceID());
        dto.setShipperID(entity.getShipperID());
        dto.setPeriodStart(entity.getPeriodStart().atStartOfDay());
        dto.setPeriodEnd(entity.getPeriodEnd().atStartOfDay());
        dto.setLinesJSON(entity.getLinesJSON());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setIssuedAt(entity.getIssuedAt());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    // ================= CREATE =================
    @Override
    public InvoiceDTO save(InvoiceDTO invoice) {
        Invoice saved = repo.save(toEntity(invoice));
        notificationClient.notifyUser(
                saved.getShipperID(),
                saved.getInvoiceID(),
                "Invoice " + saved.getInvoiceID() + " generated and pending approval.",
                "Invoice"
        );
        // WHY: billing approvals must be represented as tasks to ensure invoice review completion.
        taskClient.createTask(
            saved.getShipperID(),
            saved.getInvoiceID(),
            "Review and approve invoice " + saved.getInvoiceID() + ".",
            saved.getPeriodEnd() != null ? saved.getPeriodEnd() : LocalDate.now()
        );
        return toDTO(saved);
    }

    // ================= GET ALL =================
    @Override
    public List<InvoiceRequiredResponseDTO> getAll() {
        List<Invoice> invoices = repo.findAll();
        if (invoices.isEmpty()) {
            throw new InvoiceNotFoundException("No invoices found");
        }

        return invoices.stream().map(invoice -> {

            InvoiceDTO invoiceDTO = toDTO(invoice);
            ShipperDTO shipperDTO;

            try {
                shipperDTO = restTemplate.getForObject(
                        "http://BOOKING-SERVICE/cargoRoute/shipper/getShipper/" + invoice.getShipperID(),
                        ShipperDTO.class
                );
            } catch (Exception e) {
                shipperDTO = null;
            }

            InvoiceRequiredResponseDTO response = new InvoiceRequiredResponseDTO();
            response.setInvoice(invoiceDTO);
            response.setShipper(shipperDTO);
            return response;

        }).toList();
    }

    // ================= GET BY ID =================
    @Override
    public InvoiceRequiredResponseDTO getById(Long id) {

        Invoice invoice = repo.findById(id)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(
                                "Invoice not found with id: " + id)
                );

        InvoiceDTO invoiceDTO = toDTO(invoice);
        ShipperDTO shipperDTO;

        try {
            shipperDTO = restTemplate.getForObject(
                    "http://BOOKING-SERVICE/cargoRoute/shipper/getShipper/" + invoice.getShipperID(),
                    ShipperDTO.class
            );
        } catch (Exception e) {
            shipperDTO = null;
        }

        InvoiceRequiredResponseDTO response = new InvoiceRequiredResponseDTO();
        response.setInvoice(invoiceDTO);
        response.setShipper(shipperDTO);
        return response;
    }

    // ================= UPDATE =================
    @Override
    public InvoiceDTO update(Long id, InvoiceDTO invoiceDTO) {

        Invoice existing = repo.findById(id)
                .orElseThrow(() ->
                        new InvoiceNotFoundException(
                                "Invoice not found with id: " + id)
                );

        existing.setShipperID(invoiceDTO.getShipperID());
        existing.setPeriodStart(invoiceDTO.getPeriodStart().toLocalDate());
        existing.setPeriodEnd(invoiceDTO.getPeriodEnd().toLocalDate());
        existing.setLinesJSON(invoiceDTO.getLinesJSON());
        existing.setTotalAmount(invoiceDTO.getTotalAmount());
        existing.setStatus(invoiceDTO.getStatus());

        Invoice updated = repo.save(existing);
        notificationClient.notifyUser(
            updated.getShipperID(),
            updated.getInvoiceID(),
            "Invoice " + updated.getInvoiceID() + " updated with status " + updated.getStatus() + ".",
            "Invoice"
        );
        // WHY: invoice updates can affect settlement decisions, so they need explicit task follow-up.
        taskClient.createTask(
            updated.getShipperID(),
            updated.getInvoiceID(),
            "Reconcile updated invoice " + updated.getInvoiceID() + " with current status.",
            updated.getPeriodEnd() != null ? updated.getPeriodEnd() : LocalDate.now()
        );
        return toDTO(updated);
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {

        if (!repo.existsById(id)) {
            throw new InvoiceNotFoundException(
                    "Invoice not found with id: " + id);
        }

        repo.deleteById(id);
    }
}