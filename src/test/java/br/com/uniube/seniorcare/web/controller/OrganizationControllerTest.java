package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.service.OrganizationService;
import br.com.uniube.seniorcare.web.dto.request.OrganizationRequest;
import br.com.uniube.seniorcare.web.dto.response.OrganizationResponse;
import br.com.uniube.seniorcare.web.mapper.OrganizationMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationController organizationController;

    @Test
    void findAllReturnsListOfOrganizationsAndStatus200() {
        List<Organization> organizations = List.of(new Organization(), new Organization());
        List<OrganizationResponse> responses = List.of(new OrganizationResponse(), new OrganizationResponse());

        when(organizationService.findAll()).thenReturn(organizations);
        when(organizationMapper.toDtoList(organizations)).thenReturn(responses);

        ResponseEntity<List<OrganizationResponse>> response = organizationController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
    }

    @Test
    void findByIdReturnsOrganizationAndStatus200WhenIdExists() {
        UUID id = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationResponse response = new OrganizationResponse();

        when(organizationService.findById(id)).thenReturn(organization);
        when(organizationMapper.toDto(organization)).thenReturn(response);

        ResponseEntity<OrganizationResponse> result = organizationController.findById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void findByIdThrowsExceptionAndReturnsStatus404WhenIdDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(organizationService.findById(id)).thenThrow(new EntityNotFoundException("Organization not found"));

        assertThrows(EntityNotFoundException.class, () -> organizationController.findById(id));
    }

    @Test
    void createReturnsCreatedOrganizationAndStatus201() {
        OrganizationRequest request = new OrganizationRequest();
        Organization organization = new Organization();
        Organization createdOrganization = new Organization();
        OrganizationResponse response = new OrganizationResponse();

        when(organizationMapper.toEntity(request)).thenReturn(organization);
        when(organizationService.createOrganization(organization)).thenReturn(createdOrganization);
        when(organizationMapper.toDto(createdOrganization)).thenReturn(response);

        ResponseEntity<OrganizationResponse> result = organizationController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateReturnsUpdatedOrganizationAndStatus200() {
        UUID id = UUID.randomUUID();
        OrganizationRequest request = new OrganizationRequest();
        Organization organization = new Organization();
        Organization updatedOrganization = new Organization();
        OrganizationResponse response = new OrganizationResponse();

        when(organizationMapper.toEntity(request)).thenReturn(organization);
        when(organizationService.updateOrganization(id, organization)).thenReturn(updatedOrganization);
        when(organizationMapper.toDto(updatedOrganization)).thenReturn(response);

        ResponseEntity<OrganizationResponse> result = organizationController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteReturnsStatus204WhenOrganizationIsDeleted() {
        UUID id = UUID.randomUUID();

        doNothing().when(organizationService).deleteOrganization(id);

        ResponseEntity<Void> result = organizationController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(organizationService, times(1)).deleteOrganization(id);
    }
}