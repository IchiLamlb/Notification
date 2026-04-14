show databases;
create database notification_system;
use notification_system;

CREATE TABLE Users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW()
);

CREATE TABLE Preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    allowed_messages_priority JSON,
    INDEX (user_id),
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE Templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    placeholders JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW()
);


CREATE TABLE Notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL,
    status ENUM('pending', 'sent', 'failed') NOT NULL DEFAULT 'pending',
    message TEXT,
    message_hash char(128), -- SHA512 algo being used 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    UNIQUE(user_id, channel, message_hash),
    INDEX (user_id),
    INDEX (status),
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE delivery_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL, -- 'email', 'sms', 'push'
    status VARCHAR(20) NOT NULL, -- 'sent', 'failed', 'retrying'
    error_message TEXT, -- Optional: store error details if failed
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

alter table notifications add priority ENUM ('1', '2', '3') not null default '1';
alter table templates add template_priority ENUM ('1', '2', '3') not null default '1';


INSERT INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'OTP Verification',
    'Your OTP is {otp}. Please use this to complete your verification. Do not share this code with anyone.',
    '["otp"]',
    '1'
);
INSERT INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Welcome Greeting',
    'Hello {name}, welcome to Trigear! We are excited to have you onboard.',
    '["name"]',
    '2'
);
INSERT INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Password Reset',
    'Hi {name}, you requested to reset your password. Use the link below to set a new password: {reset_link}',
    '["name", "reset_link"]',
    '1'
);
INSERT INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Account Deactivation Warning',
    'Dear {name}, your account is scheduled for deactivation on {deactivation_date}. Please contact support if this is a mistake.',
    '["name", "deactivation_date"]',
    '1'
);
INSERT INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Birthday Wish',
    'Happy Birthday, {name}! 🎉 Wishing you a fantastic day filled with joy and laughter. Here’s a special treat: {birthday_offer}.',
    '["name", "birthday_offer"]',
    '2'
);

INSERT INTO notification_system.templates (name, content, placeholders, template_priority)
VALUES (
    'Trending Nearby',
    'Hi {name}, check out what’s trending in {location}! Don’t miss out on amazing deals and events near you.',
    '["name", "location"]',
    '3'
);

INSERT INTO notification_system.users (name, email, phone)
VALUES (
    'Lam',
    'hellomeomoc@gmail.com',
    '0793206191'
);

INSERT INTO preferences (user_id, channel, is_enabled, allowed_messages_priority, quiet_hours)
VALUES
-- Email channel preferences
(2, 'email', TRUE, '[1, 2]', '{"quietHoursEnabled": true,"start": "22:00", "end": "06:00"}'),
-- SMS channel preferences
(2, 'sms', False, '[1, 2]', '{"quietHoursEnabled": true,"start": "18:00", "end": "09:00"}'),
-- Push channel preferences
(2, 'push', True, '[3]', '{"quietHoursEnabled": false,"start": "00:00", "end": "00:00"}');

show create table notifications;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `channel` enum('email','sms','push') NOT NULL,
  `status` enum('pending','sent','failed') NOT NULL DEFAULT 'pending',
  `message` text,
  `notification_hash` char(128) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `priority` enum('1','2','3') NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`channel`,`notification_hash`),
  KEY `user_id_2` (`user_id`),
  KEY `status` (`status`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

