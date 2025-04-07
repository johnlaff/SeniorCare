package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Document;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de documentos dos idosos.
 *
 * Regras de negócio:
 * 1. Cada documento deve estar vinculado a um idoso e uma organização
 * 2. Somente usuários autorizados podem acessar documentos (Admin, Cuidadores, Familiares do idoso)
 * 3. Suporta categorização por tipo de documento
 * 4. Registro de eventos de auditoria para todas as operações
 */
public interface DocumentService {

    /**
     * Retorna todos os documentos da organização do usuário atual.
     *
     * @return lista de documentos.
     */
    List<Document> findAll();

    /**
     * Busca um documento pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do documento.
     * @return documento encontrado.
     */
    Document findById(UUID id);

    /**
     * Lista documentos de um idoso específico.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de documentos do idoso.
     */
    List<Document> findByElderly(UUID elderlyId);

    /**
     * Lista documentos por tipo de documento.
     *
     * @param documentType tipo de documento.
     * @return lista de documentos do tipo especificado.
     */
    List<Document> findByDocumentType(String documentType);

    /**
     * Lista documentos de um idoso filtrando por tipo.
     *
     * @param elderlyId identificador do idoso.
     * @param documentType tipo de documento.
     * @return lista de documentos do idoso do tipo especificado.
     */
    List<Document> findByElderlyAndDocumentType(UUID elderlyId, String documentType);

    /**
     * Faz upload de um novo documento, aplicando validações de negócio.
     *
     * @param document metadados do documento.
     * @param content conteúdo do arquivo.
     * @param fileName nome original do arquivo.
     * @return documento criado.
     */
    Document uploadDocument(Document document, InputStream content, String fileName);

    /**
     * Retorna o conteúdo de um documento para download.
     *
     * @param id identificador do documento.
     * @return stream com o conteúdo do arquivo.
     */
    InputStream downloadDocument(UUID id);

    /**
     * Atualiza metadados de um documento existente.
     *
     * @param id identificador do documento.
     * @param updatedDocument entidade com os dados atualizados.
     * @return documento atualizado.
     */
    Document updateDocument(UUID id, Document updatedDocument);

    /**
     * Exclui um documento.
     *
     * @param id identificador do documento a ser excluído.
     */
    void deleteDocument(UUID id);

    /**
     * Verifica se o usuário atual tem acesso a um documento.
     *
     * @param documentId identificador do documento.
     * @return true se tem acesso, false caso contrário.
     */
    boolean hasAccess(UUID documentId);
}