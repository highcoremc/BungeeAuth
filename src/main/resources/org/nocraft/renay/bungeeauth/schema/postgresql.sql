-- BungeeAuth PostgreSQL Schema

CREATE TABLE "{prefix}users"
(
    "id"            SERIAL PRIMARY KEY NOT NULL,
    "unique_id"     VARCHAR(36) UNIQUE NOT NULL,
    "username"      VARCHAR(36)        NOT NULL,
    "realname"      VARCHAR(36)        NOT NULL,
    "last_seen"     TIMESTAMP          NOT NULL,
    "last_seen_ip"  VARCHAR(36)        NOT NULL,
    "registered_at" TIMESTAMP          NOT NULL,
    "registered_ip" VARCHAR(36)        NOT NULL
);

CREATE TABLE "{prefix}user_password"
(
    "id"         SERIAL PRIMARY KEY NOT NULL,
    "user_id"    INT                NOT NULL,
    "unique_id"  VARCHAR(36) UNIQUE NOT NULL,
    "password"   TIMESTAMP          NOT NULL,
    "updated_at" TIMESTAMP          NOT NULL,
    "created_at" TIMESTAMP          NOT NULL
);