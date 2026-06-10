-- Admin user (password: Admin1234! — bcrypt hash, update via app if needed)
INSERT INTO users (username, email, password, role)
SELECT 'admin', 'admin@inventory.com',
       '$2a$12$7gC7bqHBHMGbNXpPCkaqyOGo7DQ8TXi.rKSdFkT7cXFbGpZqH5N0.',
       'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@inventory.com');

-- Categories
INSERT INTO categories (name, description)
SELECT 'Electronics', 'Electronic devices and accessories'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Electronics');

INSERT INTO categories (name, description)
SELECT 'Clothing', 'Apparel and fashion items'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Clothing');

INSERT INTO categories (name, description)
SELECT 'Food', 'Food and beverage products'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Food');

-- Two sample products in Electronics
INSERT INTO products (name, description, price, stock, sku, category_id, created_by_id)
SELECT 'Laptop Pro 15',
       '15-inch professional laptop with 16GB RAM',
       1299.99,
       10,
       'ELEC-001',
       (SELECT id FROM categories WHERE name = 'Electronics'),
       (SELECT id FROM users WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'ELEC-001');

INSERT INTO products (name, description, price, stock, sku, category_id, created_by_id)
SELECT 'Wireless Headphones',
       'Noise-cancelling over-ear headphones',
       199.99,
       25,
       'ELEC-002',
       (SELECT id FROM categories WHERE name = 'Electronics'),
       (SELECT id FROM users WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'ELEC-002');
