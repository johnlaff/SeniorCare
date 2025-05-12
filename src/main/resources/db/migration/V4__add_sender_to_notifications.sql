-- Adiciona o campo sender_id na tabela notifications
ALTER TABLE notifications RENAME COLUMN user_id TO receiver_id;
ALTER TABLE notifications ADD COLUMN sender_id UUID;
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES users(id);