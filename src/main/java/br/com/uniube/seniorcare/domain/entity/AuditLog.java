package br.com.uniube.seniorcare.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um registro de auditoria no sistema.
 * Cada ação crítica é persistida nessa tabela para fins de rastreabilidade.
 */
@Entity(name = "AuditLog")
@Table(name = "audit_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Momento exato em que a ação foi realizada.
     * Preenchido em código via @PrePersist, para evitar depender de DEFAULT no DB.
     */
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Popula o campo timestamp com data/hora atual antes de persistir a entidade.
     */
    @PrePersist
    public void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final AuditLog that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
