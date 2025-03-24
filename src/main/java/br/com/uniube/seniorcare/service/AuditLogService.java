package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.AuditLog;
import br.com.uniube.seniorcare.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public AuditLog findById(UUID id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit log not found with id: " + id));
    }

    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public AuditLog updateAuditLog(UUID id, AuditLog updatedAuditLog) {
        AuditLog auditLog = findById(id);
        auditLog.setAction(updatedAuditLog.getAction());
        auditLog.setEntityName(updatedAuditLog.getEntityName());
        auditLog.setEntityId(updatedAuditLog.getEntityId());
        auditLog.setTimestamp(updatedAuditLog.getTimestamp());
        auditLog.setDescription(updatedAuditLog.getDescription());
        return auditLogRepository.save(auditLog);
    }

    public void deleteAuditLog(UUID id) {
        AuditLog auditLog = findById(id);
        auditLogRepository.delete(auditLog);
    }
}
