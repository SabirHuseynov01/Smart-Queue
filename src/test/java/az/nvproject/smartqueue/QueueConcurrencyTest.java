package az.nvproject.smartqueue;

import az.nvproject.smartqueue.customers.dto.QueueResponse;
import az.nvproject.smartqueue.customers.entity.QueueCustomer;
import az.nvproject.smartqueue.customers.enums.QueueStatus;
import az.nvproject.smartqueue.customers.repository.QueueRepository;
import az.nvproject.smartqueue.customers.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QueueConcurrencyTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueRepository queueRepository;

    @BeforeEach
    void setUp() {
        queueRepository.deleteAll();
    }

    @Test
    void concurrentRequestsShouldCallDifferentCustomers()
            throws Exception {

        QueueCustomer first =
                queueRepository.save(new QueueCustomer("Sabir"));

        QueueCustomer second =
                queueRepository.save(new QueueCustomer("Ali"));

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<QueueResponse> task = () -> {

            ready.countDown();

            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Could not start concurrent requests"
                );
            }

            return queueService.callNextCustomer();
        };

        try {

            Future<QueueResponse> firstRequest =
                    executor.submit(task);

            Future<QueueResponse> secondRequest =
                    executor.submit(task);

            assertThat(ready.await(5, TimeUnit.SECONDS))
                    .isTrue();

            // Release both threads at the same time
            start.countDown();

            QueueResponse firstResponse =
                    firstRequest.get(10, TimeUnit.SECONDS);

            QueueResponse secondResponse =
                    secondRequest.get(10, TimeUnit.SECONDS);

            // Both requests must receive different customers
            assertThat(firstResponse.id())
                    .isNotEqualTo(secondResponse.id());

            assertThat(Set.of(
                    firstResponse.id(),
                    secondResponse.id()
            )).containsExactlyInAnyOrder(
                    first.getId(),
                    second.getId()
            );

            // Both customers must now be serving
            assertThat(queueRepository.findById(first.getId())
                    .orElseThrow()
                    .getStatus())
                    .isEqualTo(QueueStatus.SERVING);

            assertThat(queueRepository.findById(second.getId())
                    .orElseThrow()
                    .getStatus())
                    .isEqualTo(QueueStatus.SERVING);

        } finally {

            executor.shutdownNow();
        }
    }
}
