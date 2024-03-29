-- BungeeAuth PostgreSQL Schema

CREATE TABLE IF NOT EXISTS "{prefix}users"
(
    "id"            SERIAL PRIMARY KEY                  NOT NULL,
    "unique_id"     VARCHAR(36) UNIQUE                  NOT NULL,
    "username"      VARCHAR(36)                         NOT NULL,
    "realname"      VARCHAR(36)                         NOT NULL,
    "registered_host"      TEXT                         NOT NULL,
    "registered_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "registered_ip" VARCHAR(36)                         NOT NULL
);

CREATE TABLE IF NOT EXISTS "{prefix}user_password"
(
    "id"               SERIAL PRIMARY KEY                  NOT NULL,
    "unique_id"        VARCHAR(36) UNIQUE                  NOT NULL,
    "password"         VARCHAR(255)                        NOT NULL,
    "hash_method_type" VARCHAR(12)                         NOT NULL,
    "updated_at"       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "created_at"       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

DROP TRIGGER IF EXISTS user_password_update_date ON "{prefix}user_password";

CREATE OR REPLACE FUNCTION update_modified_column() RETURNS TRIGGER AS
$$
BEGIN
    IF row (NEW.*) IS DISTINCT FROM row (OLD.*) THEN
        NEW.updated_at = now();
        RETURN NEW;
    ELSE
        RETURN OLD;
    END IF;
END;
$$ language 'plpgsql';

CREATE TRIGGER user_password_update_date
    BEFORE UPDATE
    ON "{prefix}user_password"
    FOR ROW
EXECUTE PROCEDURE update_modified_column();