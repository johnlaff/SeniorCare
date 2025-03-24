package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public Organization findById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));
    }

    public Organization createOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(UUID id, Organization updatedOrganization) {
        Organization organization = findById(id);
        organization.setName(updatedOrganization.getName());
        organization.setDomain(updatedOrganization.getDomain());
        return organizationRepository.save(organization);
    }

    public void deleteOrganization(UUID id) {
        Organization organization = findById(id);
        organizationRepository.delete(organization);
    }
}
