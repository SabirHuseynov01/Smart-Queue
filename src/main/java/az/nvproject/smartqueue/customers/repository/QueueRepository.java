package az.nvproject.smartqueue.customers.repository;

import az.nvproject.smartqueue.customers.entity.QueueCustomer;
import az.nvproject.smartqueue.customers.enums.QueueStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface QueueRepository
        extends JpaRepository<QueueCustomer, Long> {

    List<QueueCustomer> findAllByStatusOrderByCreatedAtAscIdAsc(
            QueueStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<QueueCustomer> findFirstByStatusOrderByCreatedAtAscIdAsc(
            QueueStatus status
    );
}
