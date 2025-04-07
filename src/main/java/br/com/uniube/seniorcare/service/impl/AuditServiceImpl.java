package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.AuditLog;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.AuditLogRepository;
import br.com.uniube.seniorcare.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

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

        validateInputs(organizationId, userId, action, entityName);

        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .userId(userId)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    private void validateInputs(UUID organizationId, UUID userId, String action, String entityName) {
        if (organizationId == null) {
            throw new BusinessException("O ID da organização não pode ser nulo ao registrar auditoria.");
        }
        if (userId == null) {
            throw new BusinessException("O ID do usuário não pode ser nulo ao registrar auditoria.");
        }
        if (action == null || action.isBlank()) {
            throw new BusinessException("A ação não pode ser nula ou vazia ao registrar auditoria.");
        }
        if (entityName == null || entityName.isBlank()) {
            throw new BusinessException("O nome da entidade não pode ser nulo ou vazio ao registrar auditoria.");
        }
    }
}