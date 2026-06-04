SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS event_record;
DROP TABLE IF EXISTS event_option;
DROP TABLE IF EXISTS `event`;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS campus_location;
DROP TABLE IF EXISTS player_attribute;
DROP TABLE IF EXISTS player_profile;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE player_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    player_name VARCHAR(50) NOT NULL,
    grade VARCHAR(20) NOT NULL,
    major_type VARCHAR(50) NOT NULL,
    growth_route VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    current_week INT NOT NULL DEFAULT 1,
    current_title VARCHAR(50) DEFAULT '新生求生者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_profile_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE player_attribute (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    academic INT NOT NULL DEFAULT 60,
    health INT NOT NULL DEFAULT 70,
    money INT NOT NULL DEFAULT 80,
    social INT NOT NULL DEFAULT 50,
    skill INT NOT NULL DEFAULT 40,
    pressure INT NOT NULL DEFAULT 30,
    discipline INT NOT NULL DEFAULT 50,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_attribute_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE campus_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(50) NOT NULL,
    campus VARCHAR(50) NOT NULL,
    description TEXT,
    status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `event` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    location_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    probability INT NOT NULL DEFAULT 50,
    min_week INT NOT NULL DEFAULT 1,
    max_week INT NOT NULL DEFAULT 20,
    status INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_event_location FOREIGN KEY (location_id) REFERENCES campus_location(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE event_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    result_text TEXT NOT NULL,
    academic_change INT NOT NULL DEFAULT 0,
    health_change INT NOT NULL DEFAULT 0,
    money_change INT NOT NULL DEFAULT 0,
    social_change INT NOT NULL DEFAULT 0,
    skill_change INT NOT NULL DEFAULT 0,
    pressure_change INT NOT NULL DEFAULT 0,
    discipline_change INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_event_option_event FOREIGN KEY (event_id) REFERENCES `event`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE event_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    result_text TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_record_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_event_record_event FOREIGN KEY (event_id) REFERENCES `event`(id),
    CONSTRAINT fk_event_record_option FOREIGN KEY (option_id) REFERENCES event_option(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(10) NOT NULL,
    deadline DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reward_exp INT NOT NULL DEFAULT 0,
    description TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
