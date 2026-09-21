-- Database Schema for FocusAdapt API

CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    procrastination_profile VARCHAR(255),
    points INTEGER NOT NULL DEFAULT 0,
    streak INTEGER NOT NULL DEFAULT 0,
    last_study_date DATE,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    onboarding_completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false,
    questionnaire_completed BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS study_plans (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    total_estimated_duration_minutes INTEGER NOT NULL DEFAULT 0,
    available_hours_per_day INTEGER NOT NULL DEFAULT 2,
    start_date DATE NOT NULL,
    end_date DATE,
    available_days VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS study_tasks (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    scheduled_date DATE,
    estimated_duration_minutes INTEGER NOT NULL DEFAULT 0,
    content TEXT,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    study_plan_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_plan_id) REFERENCES study_plans(id)
);

CREATE TABLE IF NOT EXISTS content_blocks (
    id UUID NOT NULL PRIMARY KEY,
    sort_order INTEGER NOT NULL DEFAULT 0,
    block_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    estimated_minutes INTEGER NOT NULL DEFAULT 5,
    content TEXT,
    completed BOOLEAN NOT NULL DEFAULT false,
    study_task_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_task_id) REFERENCES study_tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS study_sessions (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    study_mode VARCHAR(255) NOT NULL DEFAULT 'FREE',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    effective_study_minutes INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255) NOT NULL,
    total_pauses_count INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    study_plan_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (study_plan_id) REFERENCES study_plans(id)
);

CREATE TABLE IF NOT EXISTS study_session_tasks (
    study_session_id UUID NOT NULL,
    study_task_id UUID NOT NULL,
    PRIMARY KEY (study_session_id, study_task_id),
    FOREIGN KEY (study_session_id) REFERENCES study_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (study_task_id) REFERENCES study_tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS missions (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    xp_reward INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    target_value INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS user_missions (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    mission_id UUID NOT NULL,
    current_value INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255) NOT NULL DEFAULT 'IN_PROGRESS',
    FOREIGN KEY (user_id) REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mission_id) REFERENCES missions(id)
);

CREATE TABLE IF NOT EXISTS badges (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    icon_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    badge_id UUID NOT NULL,
    unlocked_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (badge_id) REFERENCES badges(id)
);

CREATE TABLE IF NOT EXISTS points_history (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    points INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
