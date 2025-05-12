package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Document;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.DocumentRepository;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.DocumentService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    @Value("${app.document.storage-path}")
    private String storagePath;

    private final DocumentRepository documentRepository;
    private final ElderlyRepository elderlyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               ElderlyRepository elderlyRepository,
                               UserRepository userRepository,
                               AuditService auditService,
                               SecurityUtils securityUtils) {
        this.documentRepository = documentRepository;
        this.elderlyRepository = elderlyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<Document> findAll() {
        // Retorna documentos da organização atual
        UUID currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        return documentRepository.findByOrganizationId(currentUser.getOrganization().getId());
    }

    @Override
    public Document findById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento não encontrado com o id: " + id));

        // Verifica permissão de acesso
        if (!hasAccess(id)) {
            throw new BusinessException("Sem permissão para acessar este documento");
        }

        return document;
    }

    @Override
    public List<Document> findByElderly(UUID elderlyId) {
        // Verifica se o idoso existe
        if (!elderlyRepository.existsById(elderlyId)) {
            throw new BusinessException("Idoso não encontrado com o id: " + elderlyId);
        }

        // Verifica permissão de acesso ao idoso
        checkElderlyAccessPermission(elderlyId);

        return documentRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<Document> findByDocumentType(String documentType) {
        if (documentType == null || documentType.isBlank()) {
            throw new BusinessException("O tipo de documento é obrigatório para a pesquisa");
        }

        // Filtra apenas documentos da organização do usuário atual
        UUID currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        List<Document> documents = documentRepository.findByDocumentType(documentType);
        return documents.stream()
                .filter(doc -> doc.getOrganization().getId().equals(currentUser.getOrganization().getId()))
                .filter(doc -> checkAccessPermission(doc))
                .toList();
    }

    @Override
    public List<Document> findByElderlyAndDocumentType(UUID elderlyId, String documentType) {
        // Verifica se o idoso existe
        if (!elderlyRepository.existsById(elderlyId)) {
            throw new BusinessException("Idoso não encontrado com o id: " + elderlyId);
        }

        // Verifica permissão de acesso ao idoso
        checkElderlyAccessPermission(elderlyId);

        if (documentType == null || documentType.isBlank()) {
            throw new BusinessException("O tipo de documento é obrigatório para a pesquisa");
        }

        return documentRepository.findByElderlyIdAndDocumentType(elderlyId, documentType);
    }

    @Override
    public Document uploadDocument(Document document, InputStream content, String fileName) {
        validateDocumentData(document);

        // Verifica permissão de acesso ao idoso
        checkElderlyAccessPermission(document.getElderly().getId());

        try {
            // Gera ID único para o documento se não tiver
            if (document.getId() == null) {
                document.setId(UUID.randomUUID());
            }

            // Cria estrutura de diretórios se não existir
            String orgPath = document.getOrganization().getId().toString();
            String elderlyPath = document.getElderly().getId().toString();
            String directoryPath = Paths.get(storagePath, orgPath, elderlyPath).toString();

            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Normaliza o nome do arquivo e adiciona timestamp para evitar colisões
            String normalizedFileName = System.currentTimeMillis() + "_" +
                StringUtils.cleanPath(fileName).replaceAll("\\s+", "_");

            // Caminho completo do arquivo
            String filePath = Paths.get(directoryPath, normalizedFileName).toString();
            document.setFilePath(Paths.get(orgPath, elderlyPath, normalizedFileName).toString());

            // Salva o arquivo no sistema de arquivos
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = content.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // Persiste metadados no banco de dados
            Document savedDocument = documentRepository.save(document);

            auditService.recordEvent(
                document.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPLOAD_DOCUMENT",
                "Documento",
                savedDocument.getId(),
                "Documento carregado para o idoso: " + document.getElderly().getName() +
                    " - Tipo: " + document.getDocumentType()
            );

            return savedDocument;
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadDocument(UUID id) {
        Document document = findById(id);

        try {
            Path filePath = Paths.get(storagePath, document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BusinessException("Arquivo não encontrado no sistema");
            }

            auditService.recordEvent(
                document.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DOWNLOAD_DOCUMENT",
                "Documento",
                document.getId(),
                "Download de documento: " + document.getDocumentType() +
                    " para o idoso: " + document.getElderly().getName()
            );

            return new FileInputStream(filePath.toFile());
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    @Override
    public Document updateDocument(UUID id, Document updatedDocument) {
        Document document = findById(id);

        validateDocumentData(updatedDocument);

        // Não permite alterar idoso ou organização
        if (!document.getElderly().getId().equals(updatedDocument.getElderly().getId())) {
            throw new BusinessException("Não é permitido alterar o idoso de um documento");
        }

        if (!document.getOrganization().getId().equals(updatedDocument.getOrganization().getId())) {
            throw new BusinessException("Não é permitido alterar a organização de um documento");
        }

        // Atualiza metadados
        document.setDocumentType(updatedDocument.getDocumentType());

        Document updated = documentRepository.save(document);

        auditService.recordEvent(
            document.getOrganization().getId(),
            securityUtils.getCurrentUserId(),
            "UPDATE_DOCUMENT",
            "Documento",
            updated.getId(),
            "Documento atualizado: " + updated.getDocumentType() +
                " para o idoso: " + document.getElderly().getName()
        );

        return updated;
    }

    @Override
    public void deleteDocument(UUID id) {
        Document document = findById(id);

        try {
            // Remove o arquivo do sistema de arquivos
            Path filePath = Paths.get(storagePath, document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Remove metadados do banco
            documentRepository.delete(document);

            auditService.recordEvent(
                document.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_DOCUMENT",
                "Documento",
                document.getId(),
                "Documento excluído: " + document.getDocumentType() +
                    " do idoso: " + document.getElderly().getName()
            );
        } catch (IOException e) {
            throw new BusinessException("Erro ao excluir o arquivo: " + e.getMessage());
        }
    }

    @Override
    public boolean hasAccess(UUID documentId) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // Administradores têm acesso a todos os documentos da organização
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException("Documento não encontrado"));

        // Verifica se estão na mesma organização
        if (!document.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            return false;
        }

        // Cuidadores têm acesso a todos os documentos dos idosos na organização
        if (currentUser.getRole() == Role.CAREGIVER) {
            return true;
        }

        // Familiares só têm acesso aos documentos dos idosos vinculados a eles
        if (currentUser.getRole() == Role.FAMILY) {
            return documentRepository.isFamilyMemberDocument(documentId, currentUserId);
        }

        return false;
    }

    private void validateDocumentData(Document document) {
        if (document.getElderly() == null || document.getElderly().getId() == null) {
            throw new BusinessException("O idoso associado ao documento é obrigatório");
        }

        if (document.getOrganization() == null || document.getOrganization().getId() == null) {
            throw new BusinessException("A organização do documento é obrigatória");
        }

        if (document.getDocumentType() == null || document.getDocumentType().isBlank()) {
            throw new BusinessException("O tipo de documento é obrigatório");
        }

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(document.getElderly().getId())
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + document.getElderly().getId()));
    }

    private void checkElderlyAccessPermission(UUID elderlyId) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // Administradores e cuidadores têm acesso a todos os idosos
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.CAREGIVER) {
            return;
        }

        // Familiares só têm acesso aos idosos vinculados a eles
        if (currentUser.getRole() == Role.FAMILY) {
            boolean isFamilyMember = documentRepository.isFamilyMemberDocument(null, currentUserId);
            if (!isFamilyMember) {
                throw new BusinessException("Sem permissão para acessar documentos deste idoso");
            }
        }
    }

    private boolean checkAccessPermission(Document document) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        // Administradores têm acesso a todos os documentos da organização
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        // Cuidadores têm acesso a todos os documentos dos idosos na organização
        if (currentUser.getRole() == Role.CAREGIVER) {
            return true;
        }

        // Familiares só têm acesso aos documentos dos idosos vinculados a eles
        if (currentUser.getRole() == Role.FAMILY) {
            return documentRepository.isFamilyMemberDocument(document.getId(), currentUserId);
        }

        return false;
    }
}