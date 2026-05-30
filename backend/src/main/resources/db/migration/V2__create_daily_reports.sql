CREATE TABLE daily_reports (
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL REFERENCES users(id),
    report_date    DATE         NOT NULL,
    title          VARCHAR(200) NOT NULL,
    today_tasks    TEXT         NOT NULL,
    tomorrow_tasks TEXT         NOT NULL,
    impression     TEXT,
    summary        TEXT,
    is_public      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, report_date)
);
