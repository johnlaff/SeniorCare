package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Notification;
import br.com.uniube.seniorcare.domain.enums.NotificationStatus;

import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de notificações.
 *
 * Regras de negócio:
 * 1. Cada notificação tem um remetente, destinatário e uma mensagem
 * 2. Notificações são criadas inicialmente com status PENDENTE
 * 3. As notificações podem ser marcadas como LIDA pelo destinatário
 * 4. Registro de eventos de auditoria para todas as operações
 */
public interface NotificationService {

    /**
     * Retorna todas as notificações da organização do usuário atual.
     *
     * @return lista de notificações.
     */
    List<Notification> findAll();

    /**
     * Busca uma notificação pelo seu ID, lançando exceção se não encontrada.
     *
     * @param id identificador da notificação.
     * @return notificação encontrada.
     */
    Notification findById(UUID id);

    /**
     * Cria uma nova notificação.
     *
     * @param notification entidade que representa a nova notificação.
     * @return notificação criada.
     */
    Notification createNotification(Notification notification);

    /**
     * Envia uma notificação para um usuário específico.
     *
     * @param senderId ID do remetente.
     * @param receiverId ID do destinatário.
     * @param message conteúdo da mensagem.
     * @return notificação criada.
     */
    Notification sendNotification(UUID senderId, UUID receiverId, String message);

    /**
     * Marca uma notificação como lida.
     *
     * @param id identificador da notificação.
     * @return notificação atualizada.
     */
    Notification markAsRead(UUID id);

    /**
     * Exclui uma notificação.
     *
     * @param id identificador da notificação a ser excluída.
     */
    void deleteNotification(UUID id);

    /**
     * Busca notificações por destinatário.
     *
     * @param receiverId ID do usuário destinatário.
     * @return lista de notificações do destinatário.
     */
    List<Notification> findByReceiver(UUID receiverId);

    /**
     * Busca notificações não lidas por destinatário.
     *
     * @param receiverId ID do usuário destinatário.
     * @return lista de notificações não lidas.
     */
    List<Notification> findUnreadByReceiver(UUID receiverId);

    /**
     * Envia notificação para todos os familiares de um idoso.
     *
     * @param senderId ID do remetente.
     * @param elderlyId ID do idoso.
     * @param message conteúdo da mensagem.
     * @return lista de notificações criadas.
     */
    List<Notification> notifyFamilyMembers(UUID senderId, UUID elderlyId, String message);

    /**
     * Envia notificação para todos os cuidadores de um idoso.
     *
     * @param senderId ID do remetente.
     * @param elderlyId ID do idoso.
     * @param message conteúdo da mensagem.
     * @return lista de notificações criadas.
     */
    List<Notification> notifyCaregivers(UUID senderId, UUID elderlyId, String message);
}