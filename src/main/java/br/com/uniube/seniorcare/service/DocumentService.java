package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Document;
import br.com.uniube.seniorcare.domain.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document updateDocument(UUID id, Document updatedDocument) {
        Document document = findById(id);
        document.setFilePath(updatedDocument.getFilePath());
        document.setDocumentType(updatedDocument.getDocumentType());
        return documentRepository.save(document);
    }

    public void deleteDocument(UUID id) {
        Document document = findById(id);
        documentRepository.delete(document);
    }
}

