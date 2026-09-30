CREATE TABLE tasks(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    estimated_minutes INTEGER CHECK (estimated_minutes >= 0),
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMALE' CHECK (priority IN ('BASSE', 'NORMALE', 'HAUTE', 'CRITIQUE')),
    due_date DATE,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EN_PAUSE', 'TERMINEE')),
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE tags(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL COLLATE NOCASE,
    is_default BOOLEAN NOT NULL DEFAULT 0,
    is_protected BOOLEAN NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX ux_tags_name_nocase ON tags (name COLLATE NOCASE);

CREATE TABLE task_tag(
    task_id INTEGER NOT NULL,
    tag_id  INTEGER NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);
CREATE INDEX ix_task_tag_tag_id ON task_tag(tag_id);

CREATE TABLE pomodoro_sessions(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mode VARCHAR(20) NOT NULL CHECK (mode IN ('FOCUS', 'PAUSE_COURTE', 'PAUSE_LONGUE')),
    task_id INTEGER,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    actual_duration_seconds INTEGER NOT NULL CHECK (actual_duration_seconds >= 0),
    completion_status VARCHAR(20) NOT NULL CHECK (completion_status IN ('TERMINEE','INTERROMPUE')),
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
);
CREATE INDEX ix_pomodoro_sessions_started_at ON pomodoro_sessions(started_at);
CREATE INDEX ix_pomodoro_sessions_task_id ON pomodoro_sessions(task_id);

CREATE TABLE mood_entries(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE UNIQUE NOT NULL,
    mood_score INTEGER NOT NULL CHECK(mood_score BETWEEN 1 AND 7),
    physical_fatigue_score INTEGER NOT NULL CHECK (physical_fatigue_score BETWEEN 1 AND 7),
    mental_fatigue_score INTEGER NOT NULL CHECK (mental_fatigue_score BETWEEN 1 AND 7),
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE settings(
    id INTEGER PRIMARY KEY CHECK(id = 1),
    focus_duration_minutes INTEGER NOT NULL CHECK (focus_duration_minutes > 0),
    short_break_duration_minutes INTEGER NOT NULL CHECK (short_break_duration_minutes > 0),
    long_break_duration_minutes INTEGER NOT NULL CHECK (long_break_duration_minutes > 0),
    pomodoros_before_long_break INTEGER NOT NULL CHECK (pomodoros_before_long_break > 0),
    priority_coefficient_low DECIMAL(4,2) NOT NULL CHECK (priority_coefficient_low > 0),
    priority_coefficient_normal DECIMAL(4,2) NOT NULL CHECK (priority_coefficient_normal > 0),
    priority_coefficient_high DECIMAL(4,2) NOT NULL CHECK (priority_coefficient_high > 0),
    priority_coefficient_critical DECIMAL(4,2) NOT NULL CHECK (priority_coefficient_critical > 0),
    sound_notifications_enabled BOOLEAN NOT NULL
);