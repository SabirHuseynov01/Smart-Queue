package az.nvproject.smartqueue.customers.dto;

import az.nvproject.smartqueue.customers.entity.QueueCustomer;
import az.nvproject.smartqueue.customers.enums.QueueStatus;

import java.time.LocalDateTime;

public record QueueResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        QueueStatus status,
        Integer position
) {

    public static QueueResponse from(
            QueueCustomer customer,
            Integer position
    ) {
        return new QueueResponse(
                customer.getId(),
                customer.getName(),
                customer.getCreatedAt(),
                customer.getStatus(),
                position
        );
    }
}