-- Database Schema for FocusAdapt API
-- Aligned with JPA entities in com.focus.adapt.api.domain.entity

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
    questionnaire_completed BOOLEAN NOT NULL DEFAULT false,
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID NOT NULL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
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
    end_date DATE NOT NULL,
    available_days VARCHAR(255) DEFAULT 'Mon,Tue,Wed,Thu,Fri,Sat,Sun',
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
    notes TEXT,
    work_duration_minutes INTEGER NOT NULL DEFAULT 0,
    break_duration_minutes INTEGER NOT NULL DEFAULT 0,
    completed_cycles INTEGER NOT NULL DEFAULT 0,
    total_pauses_count INTEGER NOT NULL DEFAULT 0,
    xp_awarded INTEGER NOT NULL DEFAULT 0,
    quiz_bonus_xp INTEGER NOT NULL DEFAULT 0,
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
    description TEXT NOT NULL,
    points_reward INTEGER NOT NULL DEFAULT 0,
    mission_type VARCHAR(255) NOT NULL,
    target_value INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_missions (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    mission_id UUID NOT NULL,
    current_value INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(255) NOT NULL DEFAULT 'IN_PROGRESS',
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mission_id) REFERENCES missions(id)
);

CREATE TABLE IF NOT EXISTS badges (
    id UUID NOT NULL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    threshold INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL DEFAULT 0,
    icon_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    badge_id UUID NOT NULL,
    unlocked_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (badge_id) REFERENCES badges(id),
    CONSTRAINT uk_user_achievements_user_badge UNIQUE (user_id, badge_id)
);

-- Seed das badges iniciais (idempotente)
INSERT INTO badges (id, code, name, description, category, threshold, xp_reward, icon_url)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'STUDY_PLAN_1', 'Primeiro Plano', 'Conclua seu primeiro plano de estudos.', 'STUDY_PLAN', 1, 20, '/badge-icons/study-plan-1.png'),
    ('a0000000-0000-0000-0000-000000000002', 'STUDY_PLAN_3', 'Tríade de Estudos', 'Conclua 3 planos de estudos.', 'STUDY_PLAN', 3, 50, '/badge-icons/study-plan-3.png'),
    ('a0000000-0000-0000-0000-000000000003', 'STUDY_PLAN_10', 'Mestre do Planejamento', 'Conclua 10 planos de estudos.', 'STUDY_PLAN', 10, 150, '/badge-icons/study-plan-10.png'),
    ('a0000000-0000-0000-0000-000000000004', 'STUDY_PLAN_25', 'Arquiteto do Foco', 'Conclua 25 planos de estudos.', 'STUDY_PLAN', 25, 300, '/badge-icons/study-plan-25.png'),

    ('a0000000-0000-0000-0000-000000000005', 'STUDY_TASK_1', 'Primeiro Passo', 'Conclua sua primeira tarefa de estudo.', 'STUDY_TASK', 1, 20, '/badge-icons/study-task-1.png'),
    ('a0000000-0000-0000-0000-000000000006', 'STUDY_TASK_10', 'Rumo ao Progresso', 'Conclua 10 tarefas de estudo distintas.', 'STUDY_TASK', 10, 50, '/badge-icons/study-task-10.png'),
    ('a0000000-0000-0000-0000-000000000007', 'STUDY_TASK_50', 'Produtividade Pura', 'Conclua 50 tarefas de estudo distintas.', 'STUDY_TASK', 50, 150, '/badge-icons/study-task-50.png'),
    ('a0000000-0000-0000-0000-000000000008', 'STUDY_TASK_100', 'Centenário do Conhecimento', 'Conclua 100 tarefas de estudo distintas.', 'STUDY_TASK', 100, 300, '/badge-icons/study-task-100.png'),

    ('a0000000-0000-0000-0000-000000000009', 'STUDY_TIME_60', 'Hora de Ouro', 'Dedique 60 minutos de estudo com foco.', 'STUDY_TIME', 60, 20, '/badge-icons/study-time-60.png'),
    ('a0000000-0000-0000-0000-000000000010', 'STUDY_TIME_300', 'Imersão Profunda', 'Dedique 300 minutos (5 horas) de estudo com foco.', 'STUDY_TIME', 300, 50, '/badge-icons/study-time-300.png'),
    ('a0000000-0000-0000-0000-000000000011', 'STUDY_TIME_1500', 'Mente Disciplinada', 'Dedique 1.500 minutos (25 horas) de estudo com foco.', 'STUDY_TIME', 1500, 150, '/badge-icons/study-time-1500.png'),
    ('a0000000-0000-0000-0000-000000000012', 'STUDY_TIME_6000', 'Mestre da Dedicação', 'Dedique 6.000 minutos (100 horas) de estudo com foco.', 'STUDY_TIME', 6000, 300, '/badge-icons/study-time-6000.png')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    threshold = EXCLUDED.threshold,
    xp_reward = EXCLUDED.xp_reward,
    icon_url = EXCLUDED.icon_url,
    updated_at = CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS points_history (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    points INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
