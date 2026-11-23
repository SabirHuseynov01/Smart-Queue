package az.nvproject.smartqueue.customers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueueRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name
) {
}
