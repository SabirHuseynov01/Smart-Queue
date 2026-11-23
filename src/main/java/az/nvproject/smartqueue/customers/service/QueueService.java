package az.nvproject.smartqueue.customers.service;

import az.nvproject.smartqueue.commons.exception.ResourceNotFoundException;
import az.nvproject.smartqueue.customers.dto.QueueRequest;
import az.nvproject.smartqueue.customers.dto.QueueResponse;
import az.nvproject.smartqueue.customers.entity.QueueCustomer;
import az.nvproject.smartqueue.customers.enums.QueueStatus;
import az.nvproject.smartqueue.customers.repository.QueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueueService {

    private final QueueRepository queueRepository;

    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    // Add customer to queue
    @Transactional
    public QueueResponse addCustomer(QueueRequest request) {

        QueueCustomer customer = new QueueCustomer(request.name());

        QueueCustomer savedCustomer =
                queueRepository.save(customer);

        return QueueResponse.from(savedCustomer, 1);
    }

    // Get all waiting customers
    @Transactional(readOnly = true)
    public List<QueueResponse> getAllWaitingCustomers() {

        List<QueueCustomer> customers =
                queueRepository.findAllByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                );

        return toQueueResponses(customers);
    }

    // Get customer by ID
    @Transactional(readOnly = true)
    public QueueResponse getCustomerById(Long id) {

        QueueCustomer customer = findCustomerById(id);

        Integer position = null;

        if (customer.getStatus() == QueueStatus.WAITING) {

            List<QueueCustomer> waitingCustomers =
                    queueRepository.findAllByStatusOrderByCreatedAtAscIdAsc(
                            QueueStatus.WAITING
                    );

            position = findPosition(waitingCustomers, customer.getId());
        }

        return QueueResponse.from(customer, position);
    }

    // Call next customer
    @Transactional
    public QueueResponse callNextCustomer() {

        QueueCustomer customer = queueRepository
                .findFirstByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "There are no waiting customers"
                        )
                );

        customer.setStatus(QueueStatus.SERVING);

        QueueCustomer updatedCustomer =
                queueRepository.save(customer);

        return QueueResponse.from(updatedCustomer, null);
    }

    // Remove customer from queue
    @Transactional
    public void removeCustomer(Long id) {

        QueueCustomer customer = findCustomerById(id);

        if (customer.getStatus() != QueueStatus.WAITING) {
            throw new IllegalStateException(
                    "Only waiting customers can be removed"
            );
        }

        queueRepository.delete(customer);
    }

    private QueueCustomer findCustomerById(Long id) {

        return queueRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id
                        )
                );
    }

    private List<QueueResponse> toQueueResponses(
            List<QueueCustomer> customers
    ) {

        return java.util.stream.IntStream
                .range(0, customers.size())
                .mapToObj(index ->
                        QueueResponse.from(
                                customers.get(index),
                                index + 1
                        )
                )
                .toList();
    }

    private Integer findPosition(
            List<QueueCustomer> customers,
            Long customerId
    ) {
        for (int i = 0; i < customers.size(); i++) {

            if (java.util.Objects.equals(
                    customers.get(i).getId(),
                    customerId
            )) {
                return i + 1;
            }
        }

        return null;
    }
}
