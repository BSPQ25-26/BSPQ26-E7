CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

INSERT IGNORE INTO users (name, email, password, role) VALUES ('admin', 'admin@lablend.com', '$2a$12$p45dYtUAnnSUOaKVn.YJKOODJlW6QRC82Aa2SlGNWY/q1JkCZckwu', 'ADMIN');

CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    loan_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_loans_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_loans_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_equipment_id ON loans(equipment_id);