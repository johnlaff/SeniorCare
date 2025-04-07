package br.com.uniube.seniorcare.domain.entity;

    import br.com.uniube.seniorcare.domain.enums.NotificationStatus;
    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;

    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity(name = "Notification")
    @Table(name = "notifications")
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class Notification {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "organization_id", nullable = false)
        private Organization organization;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_id")
        private User sender;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "receiver_id", nullable = false)
        private User receiver;

        @Column(columnDefinition = "TEXT")
        private String message;

        @CreationTimestamp
        private LocalDateTime createdAt;

        @Enumerated(EnumType.STRING)
        private NotificationStatus status;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof final Notification that)) return false;
            return id != null && id.equals(that.getId());
        }

        @Override
        public int hashCode() {
            return 31;
        }
    }