CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    due_date DATE,
    priority VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS task_tags (
    task_id UUID NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_attachments (
    id BIGSERIAL PRIMARY KEY,
    task_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100),
    size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachments_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_completed ON tasks(completed);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_attachments_task_id ON task_attachments(task_id);
