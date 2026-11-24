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
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class QueueService {

    private final QueueRepository queueRepository;

    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    /**
     * Registers a new customer in the queue.
     */
    public QueueResponse addCustomer(QueueRequest request) {

        QueueCustomer customer = new QueueCustomer(request.name());

        QueueCustomer savedCustomer = queueRepository.save(customer);

        List<QueueCustomer> waitingCustomers =
                queueRepository.findAllByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                );

        Integer position = findPosition(
                waitingCustomers,
                savedCustomer.getId()
        );

        return QueueResponse.from(savedCustomer, position);
    }

    /**
     * Retrieves all customers currently waiting in the queue.
     */
    public List<QueueResponse> getAllWaitingCustomers() {

        List<QueueCustomer> waitingCustomers =
                queueRepository.findAllByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                );

        return toQueueResponses(waitingCustomers);
    }

    /**
     * Retrieves a customer by ID.
     */
    public QueueResponse getCustomerById(Long id) {

        QueueCustomer customer = findCustomerById(id);

        Integer position = null;

        if (customer.getStatus() == QueueStatus.WAITING) {

            List<QueueCustomer> waitingCustomers =
                    queueRepository.findAllByStatusOrderByCreatedAtAscIdAsc(
                            QueueStatus.WAITING
                    );

            position = findPosition(
                    waitingCustomers,
                    customer.getId()
            );
        }

        return QueueResponse.from(customer, position);
    }

    /**
     * Calls the next waiting customer.
     *
     * Pessimistic write locking prevents concurrent requests
     * from retrieving the same waiting customer.
     */
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

        QueueCustomer updatedCustomer = queueRepository.save(customer);

        return QueueResponse.from(updatedCustomer, null);
    }

    /**
     * Completes the service for a customer.
     *
     * Only customers with SERVING status can be completed.
     */
    @Transactional
    public QueueResponse completeCustomer(Long id) {

        QueueCustomer customer = findCustomerById(id);

        if (customer.getStatus() != QueueStatus.SERVING) {
            throw new IllegalStateException(
                    "Only customers currently being served can be completed"
            );
        }

        customer.setStatus(QueueStatus.COMPLETED);

        QueueCustomer updatedCustomer = queueRepository.save(customer);

        return QueueResponse.from(updatedCustomer, null);
    }

    /**
     * Removes a customer from the queue.
     *
     * Only customers with WAITING status can be removed.
     */
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

    /**
     * Finds a customer by ID or throws an exception.
     */
    private QueueCustomer findCustomerById(Long id) {

        return queueRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id
                        )
                );
    }

    /**
     * Converts a list of waiting customers into response DTOs.
     * Queue positions start from 1.
     */
    private List<QueueResponse> toQueueResponses(
            List<QueueCustomer> customers
    ) {

        return IntStream.range(0, customers.size())
                .mapToObj(index ->
                        QueueResponse.from(
                                customers.get(index),
                                index + 1
                        )
                )
                .toList();
    }

    /**
     * Finds the current position of a customer in the waiting list.
     */
    private Integer findPosition(
            List<QueueCustomer> customers,
            Long customerId
    ) {

        for (int i = 0; i < customers.size(); i++) {

            if (Objects.equals(
                    customers.get(i).getId(),
                    customerId
            )) {
                return i + 1;
            }
        }

        return null;
    }
}
