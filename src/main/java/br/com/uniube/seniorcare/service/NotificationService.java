package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Notification;
import br.com.uniube.seniorcare.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification findById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification updateNotification(UUID id, Notification updatedNotification) {
        Notification notification = findById(id);
        notification.setMessage(updatedNotification.getMessage());
        notification.setStatus(updatedNotification.getStatus());
        return notificationRepository.save(notification);
    }

    public void deleteNotification(UUID id) {
        Notification notification = findById(id);
        notificationRepository.delete(notification);
    }
}
