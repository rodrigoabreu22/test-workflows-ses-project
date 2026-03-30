CREATE TABLE IF NOT EXISTS pizzerias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  address VARCHAR(255),
  phone VARCHAR(30),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_pizzerias_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pizzas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  ingredients VARCHAR(800),
  pizzeria_id BIGINT NOT NULL,
  price DECIMAL(5,2) NOT NULL,
  size ENUM('small','medium','large') NOT NULL,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  photo_url TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_pizzas_pizzeria_id (pizzeria_id),
  INDEX idx_pizzas_name (name),
  CONSTRAINT fk_pizzas_pizzerias
    FOREIGN KEY (pizzeria_id) REFERENCES pizzerias(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;
