CREATE TABLE IF NOT EXISTS products (
    id             BIGSERIAL       PRIMARY KEY,
    name           VARCHAR(150)    NOT NULL,
    description    VARCHAR(1000),
    price          NUMERIC(10, 2)  NOT NULL,
    stock          INTEGER         NOT NULL DEFAULT 0,
    sku            VARCHAR(50)     NOT NULL UNIQUE,
    active         BOOLEAN         NOT NULL DEFAULT true,
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    category_id    BIGINT          REFERENCES categories(id),
    created_by_id  BIGINT          REFERENCES users(id)
);
