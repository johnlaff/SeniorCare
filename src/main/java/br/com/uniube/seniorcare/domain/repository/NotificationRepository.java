package br.com.uniube.seniorcare.domain.repository;

     import br.com.uniube.seniorcare.domain.entity.Notification;
     import br.com.uniube.seniorcare.domain.enums.NotificationStatus;
     import org.springframework.data.jpa.repository.JpaRepository;
     import java.util.List;
     import java.util.UUID;

     public interface NotificationRepository extends JpaRepository<Notification, UUID> {

         /**
          * Busca notificações por destinatário
          *
          * @param receiverId ID do destinatário
          * @return lista de notificações
          */
         List<Notification> findByReceiverId(UUID receiverId);

         /**
          * Busca notificações por destinatário e status
          *
          * @param receiverId ID do destinatário
          * @param status status da notificação
          * @return lista de notificações
          */
         List<Notification> findByReceiverIdAndStatus(UUID receiverId, NotificationStatus status);

         /**
          * Busca notificações por remetente
          *
          * @param senderId ID do remetente
          * @return lista de notificações
          */
         List<Notification> findBySenderId(UUID senderId);
     }