package az.nvproject.smartqueue.customers.entity;

    import az.nvproject.smartqueue.customers.enums.QueueStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_customers")
public class QueueCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QueueStatus status;

    protected QueueCustomer() {
    }

    public QueueCustomer(String name) {
        this.name = name;
        this.status = QueueStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = QueueStatus.WAITING;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }
}