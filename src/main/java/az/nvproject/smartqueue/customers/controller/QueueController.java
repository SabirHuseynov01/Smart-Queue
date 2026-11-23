package az.nvproject.smartqueue.customers.controller;

import az.nvproject.smartqueue.customers.dto.QueueRequest;
import az.nvproject.smartqueue.customers.dto.QueueResponse;
import az.nvproject.smartqueue.customers.service.QueueService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    // Add new customer
    @PostMapping
    public ResponseEntity<QueueResponse> addCustomer(
            @Valid @RequestBody QueueRequest request
    ) {
        QueueResponse response = queueService.addCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all waiting customers
    @GetMapping
    public ResponseEntity<List<QueueResponse>> getAllCustomers() {

        return ResponseEntity.ok(
                queueService.getAllWaitingCustomers()
        );
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<QueueResponse> getCustomerById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                queueService.getCustomerById(id)
        );
    }

    // Call next customer
    @PostMapping("/next")
    public ResponseEntity<QueueResponse> callNextCustomer() {

        return ResponseEntity.ok(
                queueService.callNextCustomer()
        );
    }

    // Remove customer from queue
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCustomer(
            @PathVariable Long id
    ) {
        queueService.removeCustomer(id);

        return ResponseEntity.noContent().build();
    }
}
