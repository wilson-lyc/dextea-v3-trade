CREATE TABLE IF NOT EXISTS api_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(128)    NOT NULL,
    name        VARCHAR(128)    NOT NULL,
    enabled     TINYINT(1)      NOT NULL DEFAULT 1,
    expire_at   DATETIME        NULL,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    UNIQUE KEY uk_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
