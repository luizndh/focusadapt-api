-- FocusAdapt API Database Schema
-- This file is automatically executed by Hibernate during startup

CREATE TABLE IF NOT EXISTS procrastination_profiles (
    id UUID NOT NULL PRIMARY KEY,
    profile_type VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    characteristics TEXT,
    tips TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS missions (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    points_reward INTEGER NOT NULL,
    mission_type VARCHAR(255) NOT NULL,
    target_value INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS badges (
    id UUID NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    icon_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS questionnaires (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    points INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    streak INTEGER NOT NULL DEFAULT 0,
    last_study_date DATE,
    onboarding_completed BOOLEAN NOT NULL DEFAULT false,
    questionnaire_completed BOOLEAN NOT NULL DEFAULT false,
    procrastination_profile_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (procrastination_profile_id) REFERENCES procrastination_profiles(id)
);

CREATE TABLE IF NOT EXISTS questions (
    id UUID NOT NULL PRIMARY KEY,
    text TEXT NOT NULL,
    order_number INTEGER NOT NULL,
    question_type VARCHAR(255) NOT NULL,
    questionnaire_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id)
);

CREATE TABLE IF NOT EXISTS answer_options (
    id UUID NOT NULL PRIMARY KEY,
    text TEXT NOT NULL,
    "value" INTEGER NOT NULL,
    procrastination_profile_type VARCHAR(255),
    question_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID NOT NULL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    type VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS study_plans (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pomodoro_sessions (
    id UUID NOT NULL PRIMARY KEY,
    work_duration_minutes INTEGER NOT NULL,
    break_duration_minutes INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    completed_cycles INTEGER NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS study_tasks (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(255) NOT NULL,
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    user_id UUID NOT NULL,
    study_plan_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (study_plan_id) REFERENCES study_plans(id)
);

CREATE TABLE IF NOT EXISTS study_sessions (
    id UUID NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    study_plan_id UUID,
    pomodoro_session_id UUID,
    study_task_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (study_plan_id) REFERENCES study_plans(id),
    FOREIGN KEY (pomodoro_session_id) REFERENCES pomodoro_sessions(id),
    FOREIGN KEY (study_task_id) REFERENCES study_tasks(id)
);

CREATE TABLE IF NOT EXISTS user_answers (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    question_id UUID NOT NULL,
    selected_option_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (selected_option_id) REFERENCES answer_options(id)
);

CREATE TABLE IF NOT EXISTS user_missions (
    id UUID NOT NULL PRIMARY KEY,
    current_value INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255) NOT NULL,
    completed_at TIMESTAMP,
    user_id UUID NOT NULL,
    mission_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mission_id) REFERENCES missions(id)
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID NOT NULL PRIMARY KEY,
    unlocked_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    badge_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (badge_id) REFERENCES badges(id)
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id UUID NOT NULL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    category VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS points_history (
    id UUID NOT NULL PRIMARY KEY,
    points INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed Default Procrastination Profiles
INSERT INTO procrastination_profiles (id, profile_type, description, characteristics, tips, created_at, updated_at, version)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'PERFECTIONIST', 'Perfil Perfeccionista: busca perfeição e teme erros.', 'Busca pela perfeição, medo de errar, autocrítica elevada.', 'Divida tarefas em etapas pequenas e comemore progressos parciais.', NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000002', 'STIMULUS_SEEKER', 'Perfil Buscador de Estímulos: busca novidades e recompensas rápidas.', 'Necessidade de novidade, busca por desafios dinâmicos, tédio com rotina.', 'Utilize ciclos Pomodoro curtos e alterne disciplinas.', NOW(), NOW(), 0)
ON CONFLICT (profile_type) DO NOTHING;

