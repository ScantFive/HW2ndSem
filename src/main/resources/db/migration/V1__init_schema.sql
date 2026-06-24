CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    due_date DATE,
    priority VARCHAR(20) NOT NULL,
    tags TEXT[] DEFAULT '{}'
);

CREATE INDEX idx_tasks_completed_priority ON tasks(completed, priority);

CREATE INDEX idx_tasks_due_date ON tasks(due_date);


CREATE TABLE IF NOT EXISTS task_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100),
    size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_attachments_task_id 
        FOREIGN KEY (task_id) 
        REFERENCES tasks(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_task_attachments_task_id ON task_attachments(task_id);

CREATE INDEX idx_task_attachments_stored_file_name ON task_attachments(stored_file_name);


CREATE TABLE IF NOT EXISTS task_tags (
    task_id  NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (task_id, tag),
    CONSTRAINT fk_task_tags_task_id
        FOREIGN KEY (task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_task_tags_tag ON task_tags(tag);
