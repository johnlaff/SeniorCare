package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
