-- Explora: catálogo de prendas de referencia y categorías de las tiendas

CREATE TABLE explore_items (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(80)  NOT NULL,
    image_url    VARCHAR(500) NOT NULL,
    image_credit VARCHAR(200),
    category     VARCHAR(20)  NOT NULL,
    color        VARCHAR(20)  NOT NULL,
    why_it_fits  VARCHAR(500) NOT NULL,
    trend        BOOLEAN      NOT NULL DEFAULT FALSE,
    accessory    BOOLEAN      NOT NULL DEFAULT FALSE,
    active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE explore_item_styles (
    item_id    BIGINT      NOT NULL REFERENCES explore_items (id) ON DELETE CASCADE,
    style_code VARCHAR(40) NOT NULL,
    PRIMARY KEY (item_id, style_code)
);
CREATE INDEX idx_explore_item_styles_code ON explore_item_styles (style_code);

CREATE TABLE explore_combine_ideas (
    item_id  BIGINT       NOT NULL REFERENCES explore_items (id) ON DELETE CASCADE,
    position INTEGER      NOT NULL,
    context  VARCHAR(40)  NOT NULL,
    items    VARCHAR(500) NOT NULL,
    PRIMARY KEY (item_id, position)
);

CREATE TABLE store_categories (
    store_id BIGINT      NOT NULL REFERENCES stores (id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL,
    PRIMARY KEY (store_id, category)
);

-- Outfit del día: no descuenta mensajes del asesor, pero tiene su propio tope diario
ALTER TABLE ai_usage ADD COLUMN daily_outfits INTEGER NOT NULL DEFAULT 0;
