SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS event_record;
DROP TABLE IF EXISTS event_option;
DROP TABLE IF EXISTS `event`;
DROP TABLE IF EXISTS user_dungeon_task_record;
DROP TABLE IF EXISTS user_dungeon_record;
DROP TABLE IF EXISTS dungeon_task_option;
DROP TABLE IF EXISTS dungeon_task;
DROP TABLE IF EXISTS dungeon;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS campus_location;
DROP TABLE IF EXISTS player_attribute;
DROP TABLE IF EXISTS player_profile;
DROP TABLE IF EXISTS user_semester_ending;
DROP TABLE IF EXISTS semester_ending;
DROP TABLE IF EXISTS user_location_exploration;
DROP TABLE IF EXISTS user_organization;
DROP TABLE IF EXISTS organization;
DROP TABLE IF EXISTS `user`;



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
    action_points INT NOT NULL DEFAULT 4,
    max_action_points INT NOT NULL DEFAULT 4,
    semester_phase VARCHAR(20) NOT NULL DEFAULT 'early',
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
    icon_key VARCHAR(50) NOT NULL DEFAULT 'map-pin',
    background_image VARCHAR(100) NOT NULL DEFAULT 'scene-campus',
    theme_color VARCHAR(20) NOT NULL DEFAULT '#2563eb',
    status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `event` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(100) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    location_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    scene_image VARCHAR(100) NOT NULL DEFAULT 'scene-campus',
    mood_tag VARCHAR(50) NOT NULL DEFAULT '日常',
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
    preview_text VARCHAR(255) NOT NULL DEFAULT '结果未知',
    risk_level VARCHAR(20) NOT NULL DEFAULT 'medium',
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

CREATE TABLE dungeon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dungeon_name VARCHAR(100) NOT NULL,
    dungeon_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    cover_image VARCHAR(100) NOT NULL DEFAULT 'scene-lab',
    theme_style VARCHAR(50) NOT NULL DEFAULT 'DDL',
    estimated_minutes INT NOT NULL DEFAULT 8,
    difficulty_label VARCHAR(50) NOT NULL DEFAULT '普通',
    reward_exp INT NOT NULL DEFAULT 0,
    reward_title VARCHAR(50),
    status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dungeon_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dungeon_id BIGINT NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    task_order INT NOT NULL,
    scene_text TEXT NOT NULL,
    target_text TEXT NOT NULL,
    background_image VARCHAR(100) NOT NULL DEFAULT 'scene-lab',
    minigame_type VARCHAR(50) NOT NULL DEFAULT 'none',
    minigame_config TEXT,
    timer_seconds INT,
    settlement_rule TEXT,
    random_enabled INT NOT NULL DEFAULT 0,
    attribute_check_rule VARCHAR(255),
    pass_condition VARCHAR(255),
    required INT NOT NULL DEFAULT 1,
    status INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_dungeon_task_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dungeon_task_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dungeon_task_id BIGINT NOT NULL,
    option_type VARCHAR(50) NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    is_correct INT NOT NULL DEFAULT 0,
    trigger_probability INT NOT NULL DEFAULT 100,
    result_text TEXT NOT NULL,
    evaluation VARCHAR(50) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    academic_change INT NOT NULL DEFAULT 0,
    health_change INT NOT NULL DEFAULT 0,
    money_change INT NOT NULL DEFAULT 0,
    social_change INT NOT NULL DEFAULT 0,
    skill_change INT NOT NULL DEFAULT 0,
    pressure_change INT NOT NULL DEFAULT 0,
    discipline_change INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    next_task_id BIGINT,
    status INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_dungeon_task_option_task FOREIGN KEY (dungeon_task_id) REFERENCES dungeon_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_dungeon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dungeon_id BIGINT NOT NULL,
    current_task_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_score INT NOT NULL DEFAULT 0,
    risk_flags TEXT,
    final_evaluation VARCHAR(100),
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME,
    CONSTRAINT fk_user_dungeon_record_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_user_dungeon_record_dungeon FOREIGN KEY (dungeon_id) REFERENCES dungeon(id),
    CONSTRAINT fk_user_dungeon_record_task FOREIGN KEY (current_task_id) REFERENCES dungeon_task(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_dungeon_task_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_dungeon_record_id BIGINT NOT NULL,
    dungeon_task_id BIGINT NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    selected_option_id BIGINT,
    random_result_id BIGINT,
    minigame_result TEXT,
    attribute_check_result VARCHAR(50),
    result_text TEXT NOT NULL,
    evaluation VARCHAR(50) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    exp_change INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_dungeon_task_record_record FOREIGN KEY (user_dungeon_record_id) REFERENCES user_dungeon_record(id),
    CONSTRAINT fk_user_dungeon_task_record_task FOREIGN KEY (dungeon_task_id) REFERENCES dungeon_task(id),
    CONSTRAINT fk_user_dungeon_task_record_option FOREIGN KEY (selected_option_id) REFERENCES dungeon_task_option(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    org_name VARCHAR(50) NOT NULL,
    org_type VARCHAR(30) NOT NULL,
    description TEXT,
    unlock_location_id BIGINT,
    unlock_explore_level INT NOT NULL DEFAULT 20,
    recommended_attribute VARCHAR(30),
    weekly_ap_cost INT NOT NULL DEFAULT 1,
    theme_color VARCHAR(20) NOT NULL DEFAULT '#2563eb',
    status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    membership_status VARCHAR(20) NOT NULL DEFAULT 'discovered',
    position_name VARCHAR(30) NOT NULL DEFAULT '成员',
    contribution INT NOT NULL DEFAULT 0,
    reputation INT NOT NULL DEFAULT 0,
    join_week INT,
    CONSTRAINT fk_user_org_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_user_org_org FOREIGN KEY (organization_id) REFERENCES organization(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_location_exploration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    explore_level INT NOT NULL DEFAULT 0,
    explore_count INT NOT NULL DEFAULT 0,
    last_explore_week INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ule_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_ule_location FOREIGN KEY (location_id) REFERENCES campus_location(id),
    UNIQUE KEY uk_user_location (user_id, location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE semester_ending (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ending_name VARCHAR(50) NOT NULL UNIQUE,
    ending_type VARCHAR(30) NOT NULL DEFAULT 'normal',
    description TEXT NOT NULL,
    condition_rule TEXT NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    theme_color VARCHAR(20) NOT NULL DEFAULT '#2563eb',
    icon VARCHAR(50) NOT NULL DEFAULT '🏆'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_semester_ending (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ending_id BIGINT NOT NULL,
    growth_route VARCHAR(50) NOT NULL,
    academic INT NOT NULL DEFAULT 0,
    health INT NOT NULL DEFAULT 0,
    social INT NOT NULL DEFAULT 0,
    skill INT NOT NULL DEFAULT 0,
    pressure INT NOT NULL DEFAULT 0,
    discipline INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_use_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_use_ending FOREIGN KEY (ending_id) REFERENCES semester_ending(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
