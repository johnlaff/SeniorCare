package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByElderlyId(UUID elderlyId);

    List<Document> findByDocumentType(String documentType);

    List<Document> findByElderlyIdAndDocumentType(UUID elderlyId, String documentType);

    List<Document> findByOrganizationId(UUID organizationId);

    @Query("SELECT DISTINCT d.documentType FROM Document d WHERE d.organization.id = :organizationId ORDER BY d.documentType")
    List<String> findAllDocumentTypes(@Param("organizationId") UUID organizationId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Document d " +
           "LEFT JOIN FamilyMember fm ON fm.elderly.id = d.elderly.id " +
           "WHERE d.id = :documentId AND fm.user.id = :userId")
    boolean isFamilyMemberDocument(@Param("documentId") UUID documentId, @Param("userId") UUID userId);
}