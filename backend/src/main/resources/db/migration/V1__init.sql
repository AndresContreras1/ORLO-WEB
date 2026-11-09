-- Esquema inicial de ORLO

CREATE TABLE styles (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(40)   NOT NULL UNIQUE,
    name        VARCHAR(80)   NOT NULL,
    description VARCHAR(1000) NOT NULL
);

CREATE TABLE style_palette (
    style_id BIGINT     NOT NULL REFERENCES styles (id) ON DELETE CASCADE,
    position INTEGER    NOT NULL,
    hex      VARCHAR(9) NOT NULL,
    PRIMARY KEY (style_id, position)
);

CREATE TABLE style_gallery (
    style_id  BIGINT       NOT NULL REFERENCES styles (id) ON DELETE CASCADE,
    position  INTEGER      NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (style_id, position)
);

CREATE TABLE quiz_questions (
    id         BIGSERIAL PRIMARY KEY,
    text       VARCHAR(300) NOT NULL,
    help_text  VARCHAR(300),
    multiple   BOOLEAN      NOT NULL,
    position   INTEGER      NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE quiz_options (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT       NOT NULL REFERENCES quiz_questions (id) ON DELETE CASCADE,
    text        VARCHAR(200) NOT NULL,
    image_url   VARCHAR(500),
    position    INTEGER      NOT NULL
);
CREATE INDEX idx_quiz_options_question ON quiz_options (question_id);

CREATE TABLE option_style_scores (
    id        BIGSERIAL PRIMARY KEY,
    option_id BIGINT  NOT NULL REFERENCES quiz_options (id) ON DELETE CASCADE,
    style_id  BIGINT  NOT NULL REFERENCES styles (id) ON DELETE CASCADE,
    points    INTEGER NOT NULL,
    CONSTRAINT uk_option_style UNIQUE (option_id, style_id)
);

CREATE TABLE lessons (
    id                   BIGSERIAL PRIMARY KEY,
    title                VARCHAR(120)  NOT NULL,
    summary              VARCHAR(300)  NOT NULL,
    main_idea            VARCHAR(2000) NOT NULL,
    tip                  VARCHAR(600)  NOT NULL,
    position             INTEGER       NOT NULL,
    minutes              INTEGER       NOT NULL,
    cover_url            VARCHAR(500),
    free_sample          BOOLEAN       NOT NULL,
    uses_color_profile   BOOLEAN       NOT NULL,
    published            BOOLEAN       NOT NULL DEFAULT TRUE,
    review_question      VARCHAR(300),
    review_correct_index INTEGER
);

CREATE TABLE lesson_review_options (
    lesson_id   BIGINT       NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    position    INTEGER      NOT NULL,
    option_text VARCHAR(200) NOT NULL,
    PRIMARY KEY (lesson_id, position)
);

CREATE TABLE lesson_examples (
    id        BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT       NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    text      VARCHAR(500) NOT NULL,
    position  INTEGER      NOT NULL
);

CREATE TABLE stores (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    logo_url      VARCHAR(500),
    garment_types VARCHAR(200) NOT NULL,
    url           VARCHAR(500) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    affiliate     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE ai_config (
    id                BIGINT PRIMARY KEY,
    system_prompt     VARCHAR(8000) NOT NULL,
    model             VARCHAR(60)   NOT NULL,
    daily_limit       INTEGER       NOT NULL,
    max_output_tokens INTEGER       NOT NULL
);

CREATE TABLE app_users (
    uid          VARCHAR(128) PRIMARY KEY,
    email        VARCHAR(200),
    premium      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ai_usage (
    id            BIGSERIAL PRIMARY KEY,
    uid           VARCHAR(128)   NOT NULL,
    usage_date    DATE           NOT NULL,
    messages      INTEGER        NOT NULL,
    input_tokens  BIGINT         NOT NULL,
    output_tokens BIGINT         NOT NULL,
    cost_usd      NUMERIC(12, 6) NOT NULL,
    CONSTRAINT uk_ai_usage_uid_date UNIQUE (uid, usage_date)
);

CREATE TABLE quiz_results (
    id             BIGSERIAL PRIMARY KEY,
    uid            VARCHAR(128) NOT NULL,
    primary_code   VARCHAR(40)  NOT NULL,
    secondary_code VARCHAR(40)  NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_quiz_results_uid ON quiz_results (uid);

CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE admin_users (
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(200) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE admin_user_roles (
    admin_user_id BIGINT NOT NULL REFERENCES admin_users (id) ON DELETE CASCADE,
    role_id       BIGINT NOT NULL REFERENCES roles (id),
    PRIMARY KEY (admin_user_id, role_id)
);
