show databases;

DROP DATABASE IF EXISTS notification_system;
create database notification_system;
use notification_system;

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       phone VARCHAR(20),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- PREFERENCES
-- =========================
CREATE TABLE preferences (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             channel ENUM('email', 'sms', 'push') NOT NULL,
                             is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                             quiet_hours JSON,
                             allowed_priorities JSON,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             UNIQUE KEY unique_user_channel (user_id, channel),

                             INDEX (user_id),
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- TEMPLATES
-- =========================
CREATE TABLE templates (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           content TEXT NOT NULL,
                           placeholders JSON,
                           priority TINYINT NOT NULL DEFAULT 1,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- NOTIFICATIONS
-- =========================
CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               channel ENUM('email', 'sms', 'push') NOT NULL,
                               status ENUM('pending', 'sent', 'failed') NOT NULL DEFAULT 'pending',
                               message TEXT,
                               message_hash CHAR(128), -- SHA512
                               priority TINYINT NOT NULL DEFAULT 1,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                               UNIQUE KEY unique_notification (user_id, channel, message_hash),

                               INDEX (user_id),
                               INDEX (status),
                               INDEX (priority),

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================
-- DELIVERY LOGS
-- =========================
CREATE TABLE delivery_logs (
                               log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               notification_id BIGINT NOT NULL,
                               channel ENUM('email', 'sms', 'push') NOT NULL,
                               status ENUM('sent', 'failed', 'retrying') NOT NULL,
                               error_message TEXT,
                               attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               INDEX (notification_id),

                               FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

alter table notifications add priority ENUM ('1', '2', '3') not null default '1';
alter table templates add template_priority ENUM ('1', '2', '3') not null default '1';

-- =========================
-- INSERT DATA
-- =========================

INSERT INTO users (name, email, phone)
VALUES (
           'Lam',
           'hellomeomoc@gmail.com',
           '0793206191'
       );

INSERT INTO preferences (user_id, channel, is_enabled, allowed_priorities, quiet_hours)
VALUES
    (1, 'email', TRUE, '[1, 2]', '{"quietHoursEnabled": true,"start": "22:00", "end": "06:00"}'),
    (1, 'sms', FALSE, '[1, 2]', '{"quietHoursEnabled": true,"start": "18:00", "end": "09:00"}'),
    (1, 'push', TRUE, '[3]', '{"quietHoursEnabled": false}');

INSERT INTO templates (name, content, placeholders, priority)
VALUES
    ('OTP Verification',
     'Your OTP is {otp}. Please use this to complete your verification.',
     '["otp"]',
     1),

    ('Welcome Greeting',
     'Hello {name}, welcome to Trigear!',
     '["name"]',
     2),

    ('Password Reset',
     'Reset your password using {reset_link}',
     '["reset_link"]',
     1),

    ('Account Deactivation Warning',
     'Account will deactivate on {date}',
     '["date"]',
     1),

    ('Birthday Wish',
     'Happy Birthday {name}!',
     '["name"]',
     2),

    ('Trending Nearby',
     'Hi {name}, trending at {location}',
     '["name", "location"]',
     3);

show create table notifications;