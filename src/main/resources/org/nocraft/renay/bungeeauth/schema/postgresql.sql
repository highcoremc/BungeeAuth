-- BungeeAuth PostgreSQL Schema

CREATE TABLE "{prefix}users" (
    "id"             UUID PRIMARY KEY NOT NULL,
    "username"       VARCHAR(200)     NOT NULL,
    "active_session" VARCHAR(36)      NOT NULL
);
CREATE INDEX "{prefix}users" ON "{prefix}users" ("id");

CREATE TABLE "{prefix}sessions" (
    "id"          UUID PRIMARY KEY NOT NULL,
    "user_id"     UUID             NOT NULL,
    "endTime"     VARCHAR(200)     NOT NULL,
    "start_time"  VARCHAR(200)     NOT NULL,
    "closed_time" VARCHAR(200)     NOT NULL
);
CREATE INDEX "{prefix}sessions_name" ON "{prefix}sessions" ("id");