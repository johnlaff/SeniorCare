package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.AuditLog;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.AuditLogRepository;
import br.com.uniube.seniorcare.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Value("${app.security.development-mode:false}")
    private boolean developmentMode;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void recordEvent(UUID organizationId,
                            UUID userId,
                            String action,
                            String entityName,
                            UUID entityId,
                            String description) {

        // Em modo de desenvolvimento, não registra auditoria
        if (developmentMode) {
            return;
        }

        validateInputs(organizationId, userId, action, entityName);

        // Create a new AuditLog instance
        AuditLog auditLog = AuditLog.builder()
                .organizationId(organizationId)
                .userId(userId)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();

        // Save the new entity
        auditLogRepository.save(auditLog);
    }

    private void validateInputs(UUID organizationId, UUID userId, String action, String entityName) {
        if (organizationId == null) {
            throw new BusinessException("OrganizationId é obrigatório para registros de auditoria");
        }
        if (userId == null) {
            throw new BusinessException("UserId é obrigatório para registros de auditoria");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new BusinessException("Action é obrigatório para registros de auditoria");
        }
        if (entityName == null || entityName.trim().isEmpty()) {
            throw new BusinessException("EntityName é obrigatório para registros de auditoria");
        }
    }
}