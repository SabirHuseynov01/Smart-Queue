package az.nvproject.smartqueue;

import az.nvproject.smartqueue.commons.exception.ResourceNotFoundException;
import az.nvproject.smartqueue.customers.dto.QueueRequest;
import az.nvproject.smartqueue.customers.dto.QueueResponse;
import az.nvproject.smartqueue.customers.entity.QueueCustomer;
import az.nvproject.smartqueue.customers.enums.QueueStatus;
import az.nvproject.smartqueue.customers.repository.QueueRepository;
import az.nvproject.smartqueue.customers.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @InjectMocks
    private QueueService queueService;

    @Test
    void shouldAddCustomerToQueue() {

        QueueCustomer customer = new QueueCustomer("Sabir");

        when(queueRepository.save(any(QueueCustomer.class)))
                .thenReturn(customer);

        when(queueRepository
                .findAllByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                ))
                .thenReturn(List.of(customer));

        QueueResponse response = queueService.addCustomer(
                new QueueRequest("Sabir")
        );

        assertThat(response.name()).isEqualTo("Sabir");
        assertThat(response.status()).isEqualTo(QueueStatus.WAITING);
        assertThat(response.position()).isEqualTo(1);

        verify(queueRepository).save(any(QueueCustomer.class));
    }

    @Test
    void shouldCallNextCustomer() {

        QueueCustomer customer = new QueueCustomer("Ali");

        when(queueRepository
                .findFirstByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                ))
                .thenReturn(Optional.of(customer));

        when(queueRepository.save(customer))
                .thenReturn(customer);

        QueueResponse response = queueService.callNextCustomer();

        assertThat(response.name()).isEqualTo("Ali");
        assertThat(response.status()).isEqualTo(QueueStatus.SERVING);

        verify(queueRepository).save(customer);
    }

    @Test
    void shouldThrowExceptionWhenQueueIsEmpty() {

        when(queueRepository
                .findFirstByStatusOrderByCreatedAtAscIdAsc(
                        QueueStatus.WAITING
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queueService.callNextCustomer())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("There are no waiting customers");
    }

    @Test
    void shouldRemoveWaitingCustomer() {

        QueueCustomer customer = new QueueCustomer("Murad");

        when(queueRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        queueService.removeCustomer(1L);

        verify(queueRepository).delete(customer);
    }

    @Test
    void shouldNotRemoveCustomerWhoIsAlreadyServing() {

        QueueCustomer customer = new QueueCustomer("Aysel");
        customer.setStatus(QueueStatus.SERVING);

        when(queueRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> queueService.removeCustomer(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only waiting customers can be removed");

        verify(queueRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        when(queueRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queueService.getCustomerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 99");
    }
}
