CREATE TABLE users (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(100)  NOT NULL,
    email                VARCHAR(255)  NOT NULL UNIQUE,
    password             VARCHAR(255)  NOT NULL,
    work_start_time      TIME          NOT NULL DEFAULT '09:00',
    notification_enabled BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
