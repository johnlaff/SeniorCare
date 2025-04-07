package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.entity.Notification;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.enums.NotificationStatus;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.FamilyMemberRepository;
import br.com.uniube.seniorcare.domain.repository.NotificationRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.domain.repository.CaregiverRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.NotificationService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ElderlyRepository elderlyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final CaregiverRepository caregiverRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   ElderlyRepository elderlyRepository,
                                   FamilyMemberRepository familyMemberRepository,
                                   CaregiverRepository caregiverRepository,
                                   AuditService auditService,
                                   SecurityUtils securityUtils) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.elderlyRepository = elderlyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.caregiverRepository = caregiverRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification findById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Notificação não encontrada com o id: " + id));
    }

    @Override
    public Notification createNotification(Notification notification) {
        validateNotificationData(notification);

        // Garante que a notificação seja criada com status PENDENTE
        notification.setStatus(NotificationStatus.PENDENTE);

        Notification createdNotification = notificationRepository.save(notification);

        auditService.recordEvent(
                createdNotification.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_NOTIFICATION",
                "Notificação",
                createdNotification.getId(),
                "Notificação criada para o usuário: " + createdNotification.getReceiver().getName()
        );

        return createdNotification;
    }

    @Override
    public Notification sendNotification(UUID senderId, UUID receiverId, String message) {
        // Verifica se o destinatário existe
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado com o id: " + receiverId));

        // Verifica se o remetente existe
        User sender = null;
        if (senderId != null) {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new BusinessException("Remetente não encontrado com o id: " + senderId));
        }

        if (message == null || message.isBlank()) {
            throw new BusinessException("A mensagem da notificação não pode ser vazia");
        }

        // Cria a notificação
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .organization(receiver.getOrganization())
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .status(NotificationStatus.PENDENTE)
                .createdAt(LocalDateTime.now())
                .build();

        Notification createdNotification = notificationRepository.save(notification);

        auditService.recordEvent(
                receiver.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "SEND_NOTIFICATION",
                "Notificação",
                createdNotification.getId(),
                "Notificação enviada para: " + receiver.getName()
        );

        return createdNotification;
    }

    @Override
    public Notification markAsRead(UUID id) {
        Notification notification = findById(id);

        if (notification.getStatus() == NotificationStatus.LIDA) {
            // Já está marcada como lida, não precisa fazer nada
            return notification;
        }

        notification.setStatus(NotificationStatus.LIDA);
        Notification updated = notificationRepository.save(notification);

        auditService.recordEvent(
                notification.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "READ_NOTIFICATION",
                "Notificação",
                notification.getId(),
                "Notificação marcada como lida"
        );

        return updated;
    }

    @Override
    public void deleteNotification(UUID id) {
        Notification notification = findById(id);

        notificationRepository.delete(notification);

        auditService.recordEvent(
                notification.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_NOTIFICATION",
                "Notificação",
                notification.getId(),
                "Notificação excluída"
        );
    }

    @Override
    public List<Notification> findByReceiver(UUID receiverId) {
        // Verifica se o usuário existe
        if (!userRepository.existsById(receiverId)) {
            throw new BusinessException("Usuário não encontrado com o id: " + receiverId);
        }

        return notificationRepository.findByReceiverId(receiverId);
    }

    @Override
    public List<Notification> findUnreadByReceiver(UUID receiverId) {
        // Verifica se o usuário existe
        if (!userRepository.existsById(receiverId)) {
            throw new BusinessException("Usuário não encontrado com o id: " + receiverId);
        }

        return notificationRepository.findByReceiverIdAndStatus(receiverId, NotificationStatus.PENDENTE);
    }

    @Override
    public List<Notification> notifyFamilyMembers(UUID senderId, UUID elderlyId, String message) {
        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        // Obtém o remetente
        User sender = null;
        if (senderId != null) {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new BusinessException("Remetente não encontrado com o id: " + senderId));
        }

        // Busca todos os familiares do idoso
        List<FamilyMember> familyMembers = familyMemberRepository.findByElderlyId(elderlyId);

        if (familyMembers.isEmpty()) {
            throw new BusinessException("O idoso não possui familiares cadastrados");
        }

        List<Notification> notifications = new ArrayList<>();

        // Envia notificação para cada familiar
        for (FamilyMember family : familyMembers) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .organization(elderly.getOrganization())
                    .sender(sender)
                    .receiver(family.getUser())
                    .message(message)
                    .status(NotificationStatus.PENDENTE)
                    .createdAt(LocalDateTime.now())
                    .build();

            notifications.add(notificationRepository.save(notification));
        }

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "NOTIFY_FAMILY_MEMBERS",
                "Notificação",
                elderly.getId(),
                "Notificação enviada para os familiares do idoso: " + elderly.getName()
        );

        return notifications;
    }

    @Override
    public List<Notification> notifyCaregivers(UUID senderId, UUID elderlyId, String message) {
        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        // Obtém o remetente
        User sender = null;
        if (senderId != null) {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new BusinessException("Remetente não encontrado com o id: " + senderId));
        }

        // Busca todos os cuidadores da organização
        List<Caregiver> allCaregivers = caregiverRepository.findByOrganizationId(elderly.getOrganization().getId());

        if (allCaregivers.isEmpty()) {
            throw new BusinessException("Não existem cuidadores na organização");
        }

        List<Notification> notifications = new ArrayList<>();

        // Envia notificação para cada cuidador
        for (Caregiver caregiver : allCaregivers) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID())
                    .organization(elderly.getOrganization())
                    .sender(sender)
                    .receiver(caregiver.getUser())
                    .message(message)
                    .status(NotificationStatus.PENDENTE)
                    .createdAt(LocalDateTime.now())
                    .build();

            notifications.add(notificationRepository.save(notification));
        }

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "NOTIFY_CAREGIVERS",
                "Notificação",
                elderly.getId(),
                "Notificação enviada para os cuidadores do idoso: " + elderly.getName()
        );

        return notifications;
    }

    private void validateNotificationData(Notification notification) {
        if (notification.getReceiver() == null || notification.getReceiver().getId() == null) {
            throw new BusinessException("O destinatário da notificação é obrigatório");
        }

        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            throw new BusinessException("A mensagem da notificação não pode ser vazia");
        }

        if (notification.getOrganization() == null || notification.getOrganization().getId() == null) {
            throw new BusinessException("A organização da notificação é obrigatória");
        }
    }
}