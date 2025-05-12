package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.OrganizationRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.OrganizationService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    // Regex para aceitar de 3 a 100 caracteres alfanuméricos ou hífens para o domínio.
    private static final Pattern DOMAIN_REGEX = Pattern.compile("^[a-zA-Z0-9-]{3,100}$");

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   AuditService auditService,
                                   SecurityUtils securityUtils) {
        this.organizationRepository = organizationRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    @Override
    public Organization findById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Organização não encontrada com o id: " + id));
    }

    @Override
    public Organization createOrganization(Organization organization) {
        // Regra: Nome único
        if (organizationRepository.existsByName(organization.getName())) {
            throw new BusinessException("Já existe uma organização com o nome '" + organization.getName() + "'.");
        }

        // Regra: Validação de formato do domínio
        if (!DOMAIN_REGEX.matcher(organization.getDomain()).matches()) {
            throw new BusinessException("Formato de domínio inválido. " +
                    "Deve conter de 3 a 100 caracteres alfanuméricos ou hífens.");
        }

        // Persistir a entidade
        Organization createdOrg = organizationRepository.save(organization);

        // Registro na auditoria
        auditService.recordEvent(
                createdOrg.getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_ORGANIZATION",
                "Organização",
                createdOrg.getId(),
                "Organização criada: " + createdOrg.getName()
        );

        return createdOrg;
    }

    @Override
    public Organization updateOrganization(UUID id, Organization updatedOrganization) {
        Organization organization = findById(id);

        // Se o nome mudou, valida unicidade
        if (!organization.getName().equals(updatedOrganization.getName())
                && organizationRepository.existsByName(updatedOrganization.getName())) {
            throw new BusinessException("Já existe outra organização com o nome '" + updatedOrganization.getName() + "'.");
        }

        // Se o domínio mudou, valida formato
        if (!organization.getDomain().equals(updatedOrganization.getDomain())) {
            if (!DOMAIN_REGEX.matcher(updatedOrganization.getDomain()).matches()) {
                throw new BusinessException("Formato de domínio inválido.");
            }
        }

        // Atualiza dados
        organization.setName(updatedOrganization.getName());
        organization.setDomain(updatedOrganization.getDomain());

        Organization updatedOrg = organizationRepository.save(organization);

        // Auditoria de atualização
        auditService.recordEvent(
                updatedOrg.getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_ORGANIZATION",
                "Organização",
                updatedOrg.getId(),
                "Organização atualizada: " + updatedOrg.getName()
        );

        return updatedOrg;
    }

    @Override
    public void deleteOrganization(UUID id) {
        Organization organization = findById(id);

        // Verifica dependentes
        if (organizationRepository.hasDependentRecords(organization.getId())) {
            throw new BusinessException("Não é possível excluir a organização; existem registros dependentes.");
        }

        organizationRepository.delete(organization);

        // Auditoria de deleção
        auditService.recordEvent(
                id,
                securityUtils.getCurrentUserId(),
                "DELETE_ORGANIZATION",
                "Organização",
                id,
                "Organização excluída com id: " + id
        );
    }
}