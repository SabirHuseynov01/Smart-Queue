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

    /**
     * Registers a new customer.
     */
    @PostMapping
    public ResponseEntity<QueueResponse> addCustomer(
            @Valid @RequestBody QueueRequest request
    ) {

        QueueResponse response = queueService.addCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves all waiting customers.
     */
    @GetMapping
    public ResponseEntity<List<QueueResponse>> getAllWaitingCustomers() {

        return ResponseEntity.ok(
                queueService.getAllWaitingCustomers()
        );
    }

    /**
     * Retrieves a customer by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QueueResponse> getCustomerById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                queueService.getCustomerById(id)
        );
    }

    /**
     * Calls the next waiting customer.
     */
    @PostMapping("/next")
    public ResponseEntity<QueueResponse> callNextCustomer() {

        return ResponseEntity.ok(
                queueService.callNextCustomer()
        );
    }

    /**
     * Completes customer service.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<QueueResponse> completeCustomer(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                queueService.completeCustomer(id)
        );
    }

    /**
     * Removes a waiting customer.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCustomer(
            @PathVariable Long id
    ) {

        queueService.removeCustomer(id);

        return ResponseEntity.noContent().build();
    }
}
